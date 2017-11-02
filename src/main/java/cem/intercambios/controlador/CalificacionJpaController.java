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
import cem.intercambios.entidades.Asignatura;
import cem.intercambios.entidades.Calificacion;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author cetecom
 */
public class CalificacionJpaController implements Serializable {

    public CalificacionJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Calificacion calificacion) throws PreexistingEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Alumno rutAlumno = calificacion.getRutAlumno();
            if (rutAlumno != null) {
                rutAlumno = em.getReference(rutAlumno.getClass(), rutAlumno.getRutPersona());
                calificacion.setRutAlumno(rutAlumno);
            }
            Asignatura codAsignatura = calificacion.getCodAsignatura();
            if (codAsignatura != null) {
                codAsignatura = em.getReference(codAsignatura.getClass(), codAsignatura.getCodigo());
                calificacion.setCodAsignatura(codAsignatura);
            }
            em.persist(calificacion);
            if (rutAlumno != null) {
                rutAlumno.getCalificacionList().add(calificacion);
                rutAlumno = em.merge(rutAlumno);
            }
            if (codAsignatura != null) {
                codAsignatura.getCalificacionList().add(calificacion);
                codAsignatura = em.merge(codAsignatura);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findCalificacion(calificacion.getCodigo()) != null) {
                throw new PreexistingEntityException("Calificacion " + calificacion + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Calificacion calificacion) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Calificacion persistentCalificacion = em.find(Calificacion.class, calificacion.getCodigo());
            Alumno rutAlumnoOld = persistentCalificacion.getRutAlumno();
            Alumno rutAlumnoNew = calificacion.getRutAlumno();
            Asignatura codAsignaturaOld = persistentCalificacion.getCodAsignatura();
            Asignatura codAsignaturaNew = calificacion.getCodAsignatura();
            if (rutAlumnoNew != null) {
                rutAlumnoNew = em.getReference(rutAlumnoNew.getClass(), rutAlumnoNew.getRutPersona());
                calificacion.setRutAlumno(rutAlumnoNew);
            }
            if (codAsignaturaNew != null) {
                codAsignaturaNew = em.getReference(codAsignaturaNew.getClass(), codAsignaturaNew.getCodigo());
                calificacion.setCodAsignatura(codAsignaturaNew);
            }
            calificacion = em.merge(calificacion);
            if (rutAlumnoOld != null && !rutAlumnoOld.equals(rutAlumnoNew)) {
                rutAlumnoOld.getCalificacionList().remove(calificacion);
                rutAlumnoOld = em.merge(rutAlumnoOld);
            }
            if (rutAlumnoNew != null && !rutAlumnoNew.equals(rutAlumnoOld)) {
                rutAlumnoNew.getCalificacionList().add(calificacion);
                rutAlumnoNew = em.merge(rutAlumnoNew);
            }
            if (codAsignaturaOld != null && !codAsignaturaOld.equals(codAsignaturaNew)) {
                codAsignaturaOld.getCalificacionList().remove(calificacion);
                codAsignaturaOld = em.merge(codAsignaturaOld);
            }
            if (codAsignaturaNew != null && !codAsignaturaNew.equals(codAsignaturaOld)) {
                codAsignaturaNew.getCalificacionList().add(calificacion);
                codAsignaturaNew = em.merge(codAsignaturaNew);
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
                BigDecimal id = calificacion.getCodigo();
                if (findCalificacion(id) == null) {
                    throw new NonexistentEntityException("The calificacion with id " + id + " no longer exists.");
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
            Calificacion calificacion;
            try {
                calificacion = em.getReference(Calificacion.class, id);
                calificacion.getCodigo();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The calificacion with id " + id + " no longer exists.", enfe);
            }
            Alumno rutAlumno = calificacion.getRutAlumno();
            if (rutAlumno != null) {
                rutAlumno.getCalificacionList().remove(calificacion);
                rutAlumno = em.merge(rutAlumno);
            }
            Asignatura codAsignatura = calificacion.getCodAsignatura();
            if (codAsignatura != null) {
                codAsignatura.getCalificacionList().remove(calificacion);
                codAsignatura = em.merge(codAsignatura);
            }
            em.remove(calificacion);
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

    public List<Calificacion> findCalificacionEntities() {
        return findCalificacionEntities(true, -1, -1);
    }

    public List<Calificacion> findCalificacionEntities(int maxResults, int firstResult) {
        return findCalificacionEntities(false, maxResults, firstResult);
    }

    private List<Calificacion> findCalificacionEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Calificacion.class));
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

    public Calificacion findCalificacion(BigDecimal id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Calificacion.class, id);
        } finally {
            em.close();
        }
    }

    public int getCalificacionCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Calificacion> rt = cq.from(Calificacion.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
