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
import cem.intercambios.entidades.Asignatura;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import cem.intercambios.entidades.Docente;
import cem.intercambios.entidades.Calificacion;
import java.util.ArrayList;
import java.util.List;
import cem.intercambios.entidades.Programa;
import java.math.BigDecimal;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author cetecom
 */
public class AsignaturaJpaController implements Serializable {

    public AsignaturaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Asignatura asignatura) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (asignatura.getCalificacionList() == null) {
            asignatura.setCalificacionList(new ArrayList<Calificacion>());
        }
        if (asignatura.getProgramaList() == null) {
            asignatura.setProgramaList(new ArrayList<Programa>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Docente rutDocente = asignatura.getRutDocente();
            if (rutDocente != null) {
                rutDocente = em.getReference(rutDocente.getClass(), rutDocente.getRutPersona());
                asignatura.setRutDocente(rutDocente);
            }
            List<Calificacion> attachedCalificacionList = new ArrayList<Calificacion>();
            for (Calificacion calificacionListCalificacionToAttach : asignatura.getCalificacionList()) {
                calificacionListCalificacionToAttach = em.getReference(calificacionListCalificacionToAttach.getClass(), calificacionListCalificacionToAttach.getCodigo());
                attachedCalificacionList.add(calificacionListCalificacionToAttach);
            }
            asignatura.setCalificacionList(attachedCalificacionList);
            List<Programa> attachedProgramaList = new ArrayList<Programa>();
            for (Programa programaListProgramaToAttach : asignatura.getProgramaList()) {
                programaListProgramaToAttach = em.getReference(programaListProgramaToAttach.getClass(), programaListProgramaToAttach.getCodigo());
                attachedProgramaList.add(programaListProgramaToAttach);
            }
            asignatura.setProgramaList(attachedProgramaList);
            em.persist(asignatura);
            if (rutDocente != null) {
                rutDocente.getAsignaturaList().add(asignatura);
                rutDocente = em.merge(rutDocente);
            }
            for (Calificacion calificacionListCalificacion : asignatura.getCalificacionList()) {
                Asignatura oldCodAsignaturaOfCalificacionListCalificacion = calificacionListCalificacion.getCodAsignatura();
                calificacionListCalificacion.setCodAsignatura(asignatura);
                calificacionListCalificacion = em.merge(calificacionListCalificacion);
                if (oldCodAsignaturaOfCalificacionListCalificacion != null) {
                    oldCodAsignaturaOfCalificacionListCalificacion.getCalificacionList().remove(calificacionListCalificacion);
                    oldCodAsignaturaOfCalificacionListCalificacion = em.merge(oldCodAsignaturaOfCalificacionListCalificacion);
                }
            }
            for (Programa programaListPrograma : asignatura.getProgramaList()) {
                Asignatura oldCodAsignaturaOfProgramaListPrograma = programaListPrograma.getCodAsignatura();
                programaListPrograma.setCodAsignatura(asignatura);
                programaListPrograma = em.merge(programaListPrograma);
                if (oldCodAsignaturaOfProgramaListPrograma != null) {
                    oldCodAsignaturaOfProgramaListPrograma.getProgramaList().remove(programaListPrograma);
                    oldCodAsignaturaOfProgramaListPrograma = em.merge(oldCodAsignaturaOfProgramaListPrograma);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findAsignatura(asignatura.getCodigo()) != null) {
                throw new PreexistingEntityException("Asignatura " + asignatura + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Asignatura asignatura) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Asignatura persistentAsignatura = em.find(Asignatura.class, asignatura.getCodigo());
            Docente rutDocenteOld = persistentAsignatura.getRutDocente();
            Docente rutDocenteNew = asignatura.getRutDocente();
            List<Calificacion> calificacionListOld = persistentAsignatura.getCalificacionList();
            List<Calificacion> calificacionListNew = asignatura.getCalificacionList();
            List<Programa> programaListOld = persistentAsignatura.getProgramaList();
            List<Programa> programaListNew = asignatura.getProgramaList();
            List<String> illegalOrphanMessages = null;
            for (Calificacion calificacionListOldCalificacion : calificacionListOld) {
                if (!calificacionListNew.contains(calificacionListOldCalificacion)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Calificacion " + calificacionListOldCalificacion + " since its codAsignatura field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (rutDocenteNew != null) {
                rutDocenteNew = em.getReference(rutDocenteNew.getClass(), rutDocenteNew.getRutPersona());
                asignatura.setRutDocente(rutDocenteNew);
            }
            List<Calificacion> attachedCalificacionListNew = new ArrayList<Calificacion>();
            for (Calificacion calificacionListNewCalificacionToAttach : calificacionListNew) {
                calificacionListNewCalificacionToAttach = em.getReference(calificacionListNewCalificacionToAttach.getClass(), calificacionListNewCalificacionToAttach.getCodigo());
                attachedCalificacionListNew.add(calificacionListNewCalificacionToAttach);
            }
            calificacionListNew = attachedCalificacionListNew;
            asignatura.setCalificacionList(calificacionListNew);
            List<Programa> attachedProgramaListNew = new ArrayList<Programa>();
            for (Programa programaListNewProgramaToAttach : programaListNew) {
                programaListNewProgramaToAttach = em.getReference(programaListNewProgramaToAttach.getClass(), programaListNewProgramaToAttach.getCodigo());
                attachedProgramaListNew.add(programaListNewProgramaToAttach);
            }
            programaListNew = attachedProgramaListNew;
            asignatura.setProgramaList(programaListNew);
            asignatura = em.merge(asignatura);
            if (rutDocenteOld != null && !rutDocenteOld.equals(rutDocenteNew)) {
                rutDocenteOld.getAsignaturaList().remove(asignatura);
                rutDocenteOld = em.merge(rutDocenteOld);
            }
            if (rutDocenteNew != null && !rutDocenteNew.equals(rutDocenteOld)) {
                rutDocenteNew.getAsignaturaList().add(asignatura);
                rutDocenteNew = em.merge(rutDocenteNew);
            }
            for (Calificacion calificacionListNewCalificacion : calificacionListNew) {
                if (!calificacionListOld.contains(calificacionListNewCalificacion)) {
                    Asignatura oldCodAsignaturaOfCalificacionListNewCalificacion = calificacionListNewCalificacion.getCodAsignatura();
                    calificacionListNewCalificacion.setCodAsignatura(asignatura);
                    calificacionListNewCalificacion = em.merge(calificacionListNewCalificacion);
                    if (oldCodAsignaturaOfCalificacionListNewCalificacion != null && !oldCodAsignaturaOfCalificacionListNewCalificacion.equals(asignatura)) {
                        oldCodAsignaturaOfCalificacionListNewCalificacion.getCalificacionList().remove(calificacionListNewCalificacion);
                        oldCodAsignaturaOfCalificacionListNewCalificacion = em.merge(oldCodAsignaturaOfCalificacionListNewCalificacion);
                    }
                }
            }
            for (Programa programaListOldPrograma : programaListOld) {
                if (!programaListNew.contains(programaListOldPrograma)) {
                    programaListOldPrograma.setCodAsignatura(null);
                    programaListOldPrograma = em.merge(programaListOldPrograma);
                }
            }
            for (Programa programaListNewPrograma : programaListNew) {
                if (!programaListOld.contains(programaListNewPrograma)) {
                    Asignatura oldCodAsignaturaOfProgramaListNewPrograma = programaListNewPrograma.getCodAsignatura();
                    programaListNewPrograma.setCodAsignatura(asignatura);
                    programaListNewPrograma = em.merge(programaListNewPrograma);
                    if (oldCodAsignaturaOfProgramaListNewPrograma != null && !oldCodAsignaturaOfProgramaListNewPrograma.equals(asignatura)) {
                        oldCodAsignaturaOfProgramaListNewPrograma.getProgramaList().remove(programaListNewPrograma);
                        oldCodAsignaturaOfProgramaListNewPrograma = em.merge(oldCodAsignaturaOfProgramaListNewPrograma);
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
                BigDecimal id = asignatura.getCodigo();
                if (findAsignatura(id) == null) {
                    throw new NonexistentEntityException("The asignatura with id " + id + " no longer exists.");
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
            Asignatura asignatura;
            try {
                asignatura = em.getReference(Asignatura.class, id);
                asignatura.getCodigo();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The asignatura with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Calificacion> calificacionListOrphanCheck = asignatura.getCalificacionList();
            for (Calificacion calificacionListOrphanCheckCalificacion : calificacionListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Asignatura (" + asignatura + ") cannot be destroyed since the Calificacion " + calificacionListOrphanCheckCalificacion + " in its calificacionList field has a non-nullable codAsignatura field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Docente rutDocente = asignatura.getRutDocente();
            if (rutDocente != null) {
                rutDocente.getAsignaturaList().remove(asignatura);
                rutDocente = em.merge(rutDocente);
            }
            List<Programa> programaList = asignatura.getProgramaList();
            for (Programa programaListPrograma : programaList) {
                programaListPrograma.setCodAsignatura(null);
                programaListPrograma = em.merge(programaListPrograma);
            }
            em.remove(asignatura);
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

    public List<Asignatura> findAsignaturaEntities() {
        return findAsignaturaEntities(true, -1, -1);
    }

    public List<Asignatura> findAsignaturaEntities(int maxResults, int firstResult) {
        return findAsignaturaEntities(false, maxResults, firstResult);
    }

    private List<Asignatura> findAsignaturaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Asignatura.class));
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

    public Asignatura findAsignatura(BigDecimal id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Asignatura.class, id);
        } finally {
            em.close();
        }
    }

    public int getAsignaturaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Asignatura> rt = cq.from(Asignatura.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
