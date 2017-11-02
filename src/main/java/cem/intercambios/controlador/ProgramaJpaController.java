/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cem.intercambios.controlador;

import cem.intercambios.controlador.exceptions.IllegalOrphanException;
import cem.intercambios.controlador.exceptions.NonexistentEntityException;
import cem.intercambios.controlador.exceptions.PreexistingEntityException;
import cem.intercambios.controlador.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import cem.intercambios.entidades.Asignatura;
import cem.intercambios.entidades.PostulacionesAlumnos;
import java.util.ArrayList;
import java.util.List;
import cem.intercambios.entidades.PostulacionesCel;
import cem.intercambios.entidades.Programa;
import java.math.BigDecimal;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author cetecom
 */
public class ProgramaJpaController implements Serializable {

    public ProgramaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Programa programa) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (programa.getPostulacionesAlumnosList() == null) {
            programa.setPostulacionesAlumnosList(new ArrayList<PostulacionesAlumnos>());
        }
        if (programa.getPostulacionesCelList() == null) {
            programa.setPostulacionesCelList(new ArrayList<PostulacionesCel>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Asignatura codAsignatura = programa.getCodAsignatura();
            if (codAsignatura != null) {
                codAsignatura = em.getReference(codAsignatura.getClass(), codAsignatura.getCodigo());
                programa.setCodAsignatura(codAsignatura);
            }
            List<PostulacionesAlumnos> attachedPostulacionesAlumnosList = new ArrayList<PostulacionesAlumnos>();
            for (PostulacionesAlumnos postulacionesAlumnosListPostulacionesAlumnosToAttach : programa.getPostulacionesAlumnosList()) {
                postulacionesAlumnosListPostulacionesAlumnosToAttach = em.getReference(postulacionesAlumnosListPostulacionesAlumnosToAttach.getClass(), postulacionesAlumnosListPostulacionesAlumnosToAttach.getCodigo());
                attachedPostulacionesAlumnosList.add(postulacionesAlumnosListPostulacionesAlumnosToAttach);
            }
            programa.setPostulacionesAlumnosList(attachedPostulacionesAlumnosList);
            List<PostulacionesCel> attachedPostulacionesCelList = new ArrayList<PostulacionesCel>();
            for (PostulacionesCel postulacionesCelListPostulacionesCelToAttach : programa.getPostulacionesCelList()) {
                postulacionesCelListPostulacionesCelToAttach = em.getReference(postulacionesCelListPostulacionesCelToAttach.getClass(), postulacionesCelListPostulacionesCelToAttach.getCodigo());
                attachedPostulacionesCelList.add(postulacionesCelListPostulacionesCelToAttach);
            }
            programa.setPostulacionesCelList(attachedPostulacionesCelList);
            em.persist(programa);
            if (codAsignatura != null) {
                codAsignatura.getProgramaList().add(programa);
                codAsignatura = em.merge(codAsignatura);
            }
            for (PostulacionesAlumnos postulacionesAlumnosListPostulacionesAlumnos : programa.getPostulacionesAlumnosList()) {
                Programa oldCodProgramaOfPostulacionesAlumnosListPostulacionesAlumnos = postulacionesAlumnosListPostulacionesAlumnos.getCodPrograma();
                postulacionesAlumnosListPostulacionesAlumnos.setCodPrograma(programa);
                postulacionesAlumnosListPostulacionesAlumnos = em.merge(postulacionesAlumnosListPostulacionesAlumnos);
                if (oldCodProgramaOfPostulacionesAlumnosListPostulacionesAlumnos != null) {
                    oldCodProgramaOfPostulacionesAlumnosListPostulacionesAlumnos.getPostulacionesAlumnosList().remove(postulacionesAlumnosListPostulacionesAlumnos);
                    oldCodProgramaOfPostulacionesAlumnosListPostulacionesAlumnos = em.merge(oldCodProgramaOfPostulacionesAlumnosListPostulacionesAlumnos);
                }
            }
            for (PostulacionesCel postulacionesCelListPostulacionesCel : programa.getPostulacionesCelList()) {
                Programa oldCodProgramaOfPostulacionesCelListPostulacionesCel = postulacionesCelListPostulacionesCel.getCodPrograma();
                postulacionesCelListPostulacionesCel.setCodPrograma(programa);
                postulacionesCelListPostulacionesCel = em.merge(postulacionesCelListPostulacionesCel);
                if (oldCodProgramaOfPostulacionesCelListPostulacionesCel != null) {
                    oldCodProgramaOfPostulacionesCelListPostulacionesCel.getPostulacionesCelList().remove(postulacionesCelListPostulacionesCel);
                    oldCodProgramaOfPostulacionesCelListPostulacionesCel = em.merge(oldCodProgramaOfPostulacionesCelListPostulacionesCel);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findPrograma(programa.getCodigo()) != null) {
                throw new PreexistingEntityException("Programa " + programa + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Programa programa) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Programa persistentPrograma = em.find(Programa.class, programa.getCodigo());
            Asignatura codAsignaturaOld = persistentPrograma.getCodAsignatura();
            Asignatura codAsignaturaNew = programa.getCodAsignatura();
            List<PostulacionesAlumnos> postulacionesAlumnosListOld = persistentPrograma.getPostulacionesAlumnosList();
            List<PostulacionesAlumnos> postulacionesAlumnosListNew = programa.getPostulacionesAlumnosList();
            List<PostulacionesCel> postulacionesCelListOld = persistentPrograma.getPostulacionesCelList();
            List<PostulacionesCel> postulacionesCelListNew = programa.getPostulacionesCelList();
            List<String> illegalOrphanMessages = null;
            for (PostulacionesAlumnos postulacionesAlumnosListOldPostulacionesAlumnos : postulacionesAlumnosListOld) {
                if (!postulacionesAlumnosListNew.contains(postulacionesAlumnosListOldPostulacionesAlumnos)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain PostulacionesAlumnos " + postulacionesAlumnosListOldPostulacionesAlumnos + " since its codPrograma field is not nullable.");
                }
            }
            for (PostulacionesCel postulacionesCelListOldPostulacionesCel : postulacionesCelListOld) {
                if (!postulacionesCelListNew.contains(postulacionesCelListOldPostulacionesCel)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain PostulacionesCel " + postulacionesCelListOldPostulacionesCel + " since its codPrograma field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (codAsignaturaNew != null) {
                codAsignaturaNew = em.getReference(codAsignaturaNew.getClass(), codAsignaturaNew.getCodigo());
                programa.setCodAsignatura(codAsignaturaNew);
            }
            List<PostulacionesAlumnos> attachedPostulacionesAlumnosListNew = new ArrayList<PostulacionesAlumnos>();
            for (PostulacionesAlumnos postulacionesAlumnosListNewPostulacionesAlumnosToAttach : postulacionesAlumnosListNew) {
                postulacionesAlumnosListNewPostulacionesAlumnosToAttach = em.getReference(postulacionesAlumnosListNewPostulacionesAlumnosToAttach.getClass(), postulacionesAlumnosListNewPostulacionesAlumnosToAttach.getCodigo());
                attachedPostulacionesAlumnosListNew.add(postulacionesAlumnosListNewPostulacionesAlumnosToAttach);
            }
            postulacionesAlumnosListNew = attachedPostulacionesAlumnosListNew;
            programa.setPostulacionesAlumnosList(postulacionesAlumnosListNew);
            List<PostulacionesCel> attachedPostulacionesCelListNew = new ArrayList<PostulacionesCel>();
            for (PostulacionesCel postulacionesCelListNewPostulacionesCelToAttach : postulacionesCelListNew) {
                postulacionesCelListNewPostulacionesCelToAttach = em.getReference(postulacionesCelListNewPostulacionesCelToAttach.getClass(), postulacionesCelListNewPostulacionesCelToAttach.getCodigo());
                attachedPostulacionesCelListNew.add(postulacionesCelListNewPostulacionesCelToAttach);
            }
            postulacionesCelListNew = attachedPostulacionesCelListNew;
            programa.setPostulacionesCelList(postulacionesCelListNew);
            programa = em.merge(programa);
            if (codAsignaturaOld != null && !codAsignaturaOld.equals(codAsignaturaNew)) {
                codAsignaturaOld.getProgramaList().remove(programa);
                codAsignaturaOld = em.merge(codAsignaturaOld);
            }
            if (codAsignaturaNew != null && !codAsignaturaNew.equals(codAsignaturaOld)) {
                codAsignaturaNew.getProgramaList().add(programa);
                codAsignaturaNew = em.merge(codAsignaturaNew);
            }
            for (PostulacionesAlumnos postulacionesAlumnosListNewPostulacionesAlumnos : postulacionesAlumnosListNew) {
                if (!postulacionesAlumnosListOld.contains(postulacionesAlumnosListNewPostulacionesAlumnos)) {
                    Programa oldCodProgramaOfPostulacionesAlumnosListNewPostulacionesAlumnos = postulacionesAlumnosListNewPostulacionesAlumnos.getCodPrograma();
                    postulacionesAlumnosListNewPostulacionesAlumnos.setCodPrograma(programa);
                    postulacionesAlumnosListNewPostulacionesAlumnos = em.merge(postulacionesAlumnosListNewPostulacionesAlumnos);
                    if (oldCodProgramaOfPostulacionesAlumnosListNewPostulacionesAlumnos != null && !oldCodProgramaOfPostulacionesAlumnosListNewPostulacionesAlumnos.equals(programa)) {
                        oldCodProgramaOfPostulacionesAlumnosListNewPostulacionesAlumnos.getPostulacionesAlumnosList().remove(postulacionesAlumnosListNewPostulacionesAlumnos);
                        oldCodProgramaOfPostulacionesAlumnosListNewPostulacionesAlumnos = em.merge(oldCodProgramaOfPostulacionesAlumnosListNewPostulacionesAlumnos);
                    }
                }
            }
            for (PostulacionesCel postulacionesCelListNewPostulacionesCel : postulacionesCelListNew) {
                if (!postulacionesCelListOld.contains(postulacionesCelListNewPostulacionesCel)) {
                    Programa oldCodProgramaOfPostulacionesCelListNewPostulacionesCel = postulacionesCelListNewPostulacionesCel.getCodPrograma();
                    postulacionesCelListNewPostulacionesCel.setCodPrograma(programa);
                    postulacionesCelListNewPostulacionesCel = em.merge(postulacionesCelListNewPostulacionesCel);
                    if (oldCodProgramaOfPostulacionesCelListNewPostulacionesCel != null && !oldCodProgramaOfPostulacionesCelListNewPostulacionesCel.equals(programa)) {
                        oldCodProgramaOfPostulacionesCelListNewPostulacionesCel.getPostulacionesCelList().remove(postulacionesCelListNewPostulacionesCel);
                        oldCodProgramaOfPostulacionesCelListNewPostulacionesCel = em.merge(oldCodProgramaOfPostulacionesCelListNewPostulacionesCel);
                    }
                }
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
                BigDecimal id = programa.getCodigo();
                if (findPrograma(id) == null) {
                    throw new NonexistentEntityException("The programa with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(BigDecimal id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Programa programa;
            try {
                programa = em.getReference(Programa.class, id);
                programa.getCodigo();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The programa with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<PostulacionesAlumnos> postulacionesAlumnosListOrphanCheck = programa.getPostulacionesAlumnosList();
            for (PostulacionesAlumnos postulacionesAlumnosListOrphanCheckPostulacionesAlumnos : postulacionesAlumnosListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Programa (" + programa + ") cannot be destroyed since the PostulacionesAlumnos " + postulacionesAlumnosListOrphanCheckPostulacionesAlumnos + " in its postulacionesAlumnosList field has a non-nullable codPrograma field.");
            }
            List<PostulacionesCel> postulacionesCelListOrphanCheck = programa.getPostulacionesCelList();
            for (PostulacionesCel postulacionesCelListOrphanCheckPostulacionesCel : postulacionesCelListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Programa (" + programa + ") cannot be destroyed since the PostulacionesCel " + postulacionesCelListOrphanCheckPostulacionesCel + " in its postulacionesCelList field has a non-nullable codPrograma field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Asignatura codAsignatura = programa.getCodAsignatura();
            if (codAsignatura != null) {
                codAsignatura.getProgramaList().remove(programa);
                codAsignatura = em.merge(codAsignatura);
            }
            em.remove(programa);
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

    public List<Programa> findProgramaEntities() {
        return findProgramaEntities(true, -1, -1);
    }

    public List<Programa> findProgramaEntities(int maxResults, int firstResult) {
        return findProgramaEntities(false, maxResults, firstResult);
    }

    private List<Programa> findProgramaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Programa.class));
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

    public Programa findPrograma(BigDecimal id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Programa.class, id);
        } finally {
            em.close();
        }
    }

    public int getProgramaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Programa> rt = cq.from(Programa.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
