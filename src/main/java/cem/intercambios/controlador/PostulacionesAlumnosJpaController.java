/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cem.intercambios.controlador;

import cem.intercambios.controlador.exceptions.NonexistentEntityException;
import cem.intercambios.controlador.exceptions.PreexistingEntityException;
import cem.intercambios.controlador.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import cem.intercambios.entidades.Alumno;
import cem.intercambios.entidades.PostulacionesAlumnos;
import cem.intercambios.entidades.Programa;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author cetecom
 */
public class PostulacionesAlumnosJpaController implements Serializable {

    public PostulacionesAlumnosJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(PostulacionesAlumnos postulacionesAlumnos) throws PreexistingEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Alumno rutAlumno = postulacionesAlumnos.getRutAlumno();
            if (rutAlumno != null) {
                rutAlumno = em.getReference(rutAlumno.getClass(), rutAlumno.getRutPersona());
                postulacionesAlumnos.setRutAlumno(rutAlumno);
            }
            Programa codPrograma = postulacionesAlumnos.getCodPrograma();
            if (codPrograma != null) {
                codPrograma = em.getReference(codPrograma.getClass(), codPrograma.getCodigo());
                postulacionesAlumnos.setCodPrograma(codPrograma);
            }
            em.persist(postulacionesAlumnos);
            if (rutAlumno != null) {
                rutAlumno.getPostulacionesAlumnosList().add(postulacionesAlumnos);
                rutAlumno = em.merge(rutAlumno);
            }
            if (codPrograma != null) {
                codPrograma.getPostulacionesAlumnosList().add(postulacionesAlumnos);
                codPrograma = em.merge(codPrograma);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findPostulacionesAlumnos(postulacionesAlumnos.getCodigo()) != null) {
                throw new PreexistingEntityException("PostulacionesAlumnos " + postulacionesAlumnos + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(PostulacionesAlumnos postulacionesAlumnos) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            PostulacionesAlumnos persistentPostulacionesAlumnos = em.find(PostulacionesAlumnos.class, postulacionesAlumnos.getCodigo());
            Alumno rutAlumnoOld = persistentPostulacionesAlumnos.getRutAlumno();
            Alumno rutAlumnoNew = postulacionesAlumnos.getRutAlumno();
            Programa codProgramaOld = persistentPostulacionesAlumnos.getCodPrograma();
            Programa codProgramaNew = postulacionesAlumnos.getCodPrograma();
            if (rutAlumnoNew != null) {
                rutAlumnoNew = em.getReference(rutAlumnoNew.getClass(), rutAlumnoNew.getRutPersona());
                postulacionesAlumnos.setRutAlumno(rutAlumnoNew);
            }
            if (codProgramaNew != null) {
                codProgramaNew = em.getReference(codProgramaNew.getClass(), codProgramaNew.getCodigo());
                postulacionesAlumnos.setCodPrograma(codProgramaNew);
            }
            postulacionesAlumnos = em.merge(postulacionesAlumnos);
            if (rutAlumnoOld != null && !rutAlumnoOld.equals(rutAlumnoNew)) {
                rutAlumnoOld.getPostulacionesAlumnosList().remove(postulacionesAlumnos);
                rutAlumnoOld = em.merge(rutAlumnoOld);
            }
            if (rutAlumnoNew != null && !rutAlumnoNew.equals(rutAlumnoOld)) {
                rutAlumnoNew.getPostulacionesAlumnosList().add(postulacionesAlumnos);
                rutAlumnoNew = em.merge(rutAlumnoNew);
            }
            if (codProgramaOld != null && !codProgramaOld.equals(codProgramaNew)) {
                codProgramaOld.getPostulacionesAlumnosList().remove(postulacionesAlumnos);
                codProgramaOld = em.merge(codProgramaOld);
            }
            if (codProgramaNew != null && !codProgramaNew.equals(codProgramaOld)) {
                codProgramaNew.getPostulacionesAlumnosList().add(postulacionesAlumnos);
                codProgramaNew = em.merge(codProgramaNew);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                BigDecimal id = postulacionesAlumnos.getCodigo();
                if (findPostulacionesAlumnos(id) == null) {
                    throw new NonexistentEntityException("The postulacionesAlumnos with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(BigDecimal id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            PostulacionesAlumnos postulacionesAlumnos;
            try {
                postulacionesAlumnos = em.getReference(PostulacionesAlumnos.class, id);
                postulacionesAlumnos.getCodigo();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The postulacionesAlumnos with id " + id + " no longer exists.", enfe);
            }
            Alumno rutAlumno = postulacionesAlumnos.getRutAlumno();
            if (rutAlumno != null) {
                rutAlumno.getPostulacionesAlumnosList().remove(postulacionesAlumnos);
                rutAlumno = em.merge(rutAlumno);
            }
            Programa codPrograma = postulacionesAlumnos.getCodPrograma();
            if (codPrograma != null) {
                codPrograma.getPostulacionesAlumnosList().remove(postulacionesAlumnos);
                codPrograma = em.merge(codPrograma);
            }
            em.remove(postulacionesAlumnos);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<PostulacionesAlumnos> findPostulacionesAlumnosEntities() {
        return findPostulacionesAlumnosEntities(true, -1, -1);
    }

    public List<PostulacionesAlumnos> findPostulacionesAlumnosEntities(int maxResults, int firstResult) {
        return findPostulacionesAlumnosEntities(false, maxResults, firstResult);
    }

    private List<PostulacionesAlumnos> findPostulacionesAlumnosEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(PostulacionesAlumnos.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public PostulacionesAlumnos findPostulacionesAlumnos(BigDecimal id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(PostulacionesAlumnos.class, id);
        } finally {
            em.close();
        }
    }

    public int getPostulacionesAlumnosCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<PostulacionesAlumnos> rt = cq.from(PostulacionesAlumnos.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
