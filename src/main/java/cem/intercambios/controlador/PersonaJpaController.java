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
import cem.intercambios.entidades.FamiliaAnfitriona;
import cem.intercambios.entidades.CentroEstudiosLocal;
import cem.intercambios.entidades.Alumno;
import cem.intercambios.entidades.Usuario;
import cem.intercambios.entidades.Docente;
import cem.intercambios.entidades.Persona;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author cetecom
 */
public class PersonaJpaController implements Serializable {

    public PersonaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Persona persona) throws PreexistingEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            FamiliaAnfitriona familiaAnfitriona = persona.getFamiliaAnfitriona();
            if (familiaAnfitriona != null) {
                familiaAnfitriona = em.getReference(familiaAnfitriona.getClass(), familiaAnfitriona.getRutPersona());
                persona.setFamiliaAnfitriona(familiaAnfitriona);
            }
            CentroEstudiosLocal centroEstudiosLocal = persona.getCentroEstudiosLocal();
            if (centroEstudiosLocal != null) {
                centroEstudiosLocal = em.getReference(centroEstudiosLocal.getClass(), centroEstudiosLocal.getRutPersona());
                persona.setCentroEstudiosLocal(centroEstudiosLocal);
            }
            Alumno alumno = persona.getAlumno();
            if (alumno != null) {
                alumno = em.getReference(alumno.getClass(), alumno.getRutPersona());
                persona.setAlumno(alumno);
            }
            Usuario usuario = persona.getUsuario();
            if (usuario != null) {
                usuario = em.getReference(usuario.getClass(), usuario.getCodigo());
                persona.setUsuario(usuario);
            }
            Docente docente = persona.getDocente();
            if (docente != null) {
                docente = em.getReference(docente.getClass(), docente.getRutPersona());
                persona.setDocente(docente);
            }
            em.persist(persona);
            if (familiaAnfitriona != null) {
                Persona oldPersonaOfFamiliaAnfitriona = familiaAnfitriona.getPersona();
                if (oldPersonaOfFamiliaAnfitriona != null) {
                    oldPersonaOfFamiliaAnfitriona.setFamiliaAnfitriona(null);
                    oldPersonaOfFamiliaAnfitriona = em.merge(oldPersonaOfFamiliaAnfitriona);
                }
                familiaAnfitriona.setPersona(persona);
                familiaAnfitriona = em.merge(familiaAnfitriona);
            }
            if (centroEstudiosLocal != null) {
                Persona oldPersonaOfCentroEstudiosLocal = centroEstudiosLocal.getPersona();
                if (oldPersonaOfCentroEstudiosLocal != null) {
                    oldPersonaOfCentroEstudiosLocal.setCentroEstudiosLocal(null);
                    oldPersonaOfCentroEstudiosLocal = em.merge(oldPersonaOfCentroEstudiosLocal);
                }
                centroEstudiosLocal.setPersona(persona);
                centroEstudiosLocal = em.merge(centroEstudiosLocal);
            }
            if (alumno != null) {
                Persona oldPersonaOfAlumno = alumno.getPersona();
                if (oldPersonaOfAlumno != null) {
                    oldPersonaOfAlumno.setAlumno(null);
                    oldPersonaOfAlumno = em.merge(oldPersonaOfAlumno);
                }
                alumno.setPersona(persona);
                alumno = em.merge(alumno);
            }
            if (usuario != null) {
                Persona oldRutPersonaOfUsuario = usuario.getRutPersona();
                if (oldRutPersonaOfUsuario != null) {
                    oldRutPersonaOfUsuario.setUsuario(null);
                    oldRutPersonaOfUsuario = em.merge(oldRutPersonaOfUsuario);
                }
                usuario.setRutPersona(persona);
                usuario = em.merge(usuario);
            }
            if (docente != null) {
                Persona oldPersonaOfDocente = docente.getPersona();
                if (oldPersonaOfDocente != null) {
                    oldPersonaOfDocente.setDocente(null);
                    oldPersonaOfDocente = em.merge(oldPersonaOfDocente);
                }
                docente.setPersona(persona);
                docente = em.merge(docente);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findPersona(persona.getRut()) != null) {
                throw new PreexistingEntityException("Persona " + persona + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Persona persona) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Persona persistentPersona = em.find(Persona.class, persona.getRut());
            FamiliaAnfitriona familiaAnfitrionaOld = persistentPersona.getFamiliaAnfitriona();
            FamiliaAnfitriona familiaAnfitrionaNew = persona.getFamiliaAnfitriona();
            CentroEstudiosLocal centroEstudiosLocalOld = persistentPersona.getCentroEstudiosLocal();
            CentroEstudiosLocal centroEstudiosLocalNew = persona.getCentroEstudiosLocal();
            Alumno alumnoOld = persistentPersona.getAlumno();
            Alumno alumnoNew = persona.getAlumno();
            Usuario usuarioOld = persistentPersona.getUsuario();
            Usuario usuarioNew = persona.getUsuario();
            Docente docenteOld = persistentPersona.getDocente();
            Docente docenteNew = persona.getDocente();
            List<String> illegalOrphanMessages = null;
            if (familiaAnfitrionaOld != null && !familiaAnfitrionaOld.equals(familiaAnfitrionaNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain FamiliaAnfitriona " + familiaAnfitrionaOld + " since its persona field is not nullable.");
            }
            if (centroEstudiosLocalOld != null && !centroEstudiosLocalOld.equals(centroEstudiosLocalNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain CentroEstudiosLocal " + centroEstudiosLocalOld + " since its persona field is not nullable.");
            }
            if (alumnoOld != null && !alumnoOld.equals(alumnoNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Alumno " + alumnoOld + " since its persona field is not nullable.");
            }
            if (usuarioOld != null && !usuarioOld.equals(usuarioNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Usuario " + usuarioOld + " since its rutPersona field is not nullable.");
            }
            if (docenteOld != null && !docenteOld.equals(docenteNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Docente " + docenteOld + " since its persona field is not nullable.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (familiaAnfitrionaNew != null) {
                familiaAnfitrionaNew = em.getReference(familiaAnfitrionaNew.getClass(), familiaAnfitrionaNew.getRutPersona());
                persona.setFamiliaAnfitriona(familiaAnfitrionaNew);
            }
            if (centroEstudiosLocalNew != null) {
                centroEstudiosLocalNew = em.getReference(centroEstudiosLocalNew.getClass(), centroEstudiosLocalNew.getRutPersona());
                persona.setCentroEstudiosLocal(centroEstudiosLocalNew);
            }
            if (alumnoNew != null) {
                alumnoNew = em.getReference(alumnoNew.getClass(), alumnoNew.getRutPersona());
                persona.setAlumno(alumnoNew);
            }
            if (usuarioNew != null) {
                usuarioNew = em.getReference(usuarioNew.getClass(), usuarioNew.getCodigo());
                persona.setUsuario(usuarioNew);
            }
            if (docenteNew != null) {
                docenteNew = em.getReference(docenteNew.getClass(), docenteNew.getRutPersona());
                persona.setDocente(docenteNew);
            }
            persona = em.merge(persona);
            if (familiaAnfitrionaNew != null && !familiaAnfitrionaNew.equals(familiaAnfitrionaOld)) {
                Persona oldPersonaOfFamiliaAnfitriona = familiaAnfitrionaNew.getPersona();
                if (oldPersonaOfFamiliaAnfitriona != null) {
                    oldPersonaOfFamiliaAnfitriona.setFamiliaAnfitriona(null);
                    oldPersonaOfFamiliaAnfitriona = em.merge(oldPersonaOfFamiliaAnfitriona);
                }
                familiaAnfitrionaNew.setPersona(persona);
                familiaAnfitrionaNew = em.merge(familiaAnfitrionaNew);
            }
            if (centroEstudiosLocalNew != null && !centroEstudiosLocalNew.equals(centroEstudiosLocalOld)) {
                Persona oldPersonaOfCentroEstudiosLocal = centroEstudiosLocalNew.getPersona();
                if (oldPersonaOfCentroEstudiosLocal != null) {
                    oldPersonaOfCentroEstudiosLocal.setCentroEstudiosLocal(null);
                    oldPersonaOfCentroEstudiosLocal = em.merge(oldPersonaOfCentroEstudiosLocal);
                }
                centroEstudiosLocalNew.setPersona(persona);
                centroEstudiosLocalNew = em.merge(centroEstudiosLocalNew);
            }
            if (alumnoNew != null && !alumnoNew.equals(alumnoOld)) {
                Persona oldPersonaOfAlumno = alumnoNew.getPersona();
                if (oldPersonaOfAlumno != null) {
                    oldPersonaOfAlumno.setAlumno(null);
                    oldPersonaOfAlumno = em.merge(oldPersonaOfAlumno);
                }
                alumnoNew.setPersona(persona);
                alumnoNew = em.merge(alumnoNew);
            }
            if (usuarioNew != null && !usuarioNew.equals(usuarioOld)) {
                Persona oldRutPersonaOfUsuario = usuarioNew.getRutPersona();
                if (oldRutPersonaOfUsuario != null) {
                    oldRutPersonaOfUsuario.setUsuario(null);
                    oldRutPersonaOfUsuario = em.merge(oldRutPersonaOfUsuario);
                }
                usuarioNew.setRutPersona(persona);
                usuarioNew = em.merge(usuarioNew);
            }
            if (docenteNew != null && !docenteNew.equals(docenteOld)) {
                Persona oldPersonaOfDocente = docenteNew.getPersona();
                if (oldPersonaOfDocente != null) {
                    oldPersonaOfDocente.setDocente(null);
                    oldPersonaOfDocente = em.merge(oldPersonaOfDocente);
                }
                docenteNew.setPersona(persona);
                docenteNew = em.merge(docenteNew);
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
                Integer id = persona.getRut();
                if (findPersona(id) == null) {
                    throw new NonexistentEntityException("The persona with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Persona persona;
            try {
                persona = em.getReference(Persona.class, id);
                persona.getRut();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The persona with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            FamiliaAnfitriona familiaAnfitrionaOrphanCheck = persona.getFamiliaAnfitriona();
            if (familiaAnfitrionaOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Persona (" + persona + ") cannot be destroyed since the FamiliaAnfitriona " + familiaAnfitrionaOrphanCheck + " in its familiaAnfitriona field has a non-nullable persona field.");
            }
            CentroEstudiosLocal centroEstudiosLocalOrphanCheck = persona.getCentroEstudiosLocal();
            if (centroEstudiosLocalOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Persona (" + persona + ") cannot be destroyed since the CentroEstudiosLocal " + centroEstudiosLocalOrphanCheck + " in its centroEstudiosLocal field has a non-nullable persona field.");
            }
            Alumno alumnoOrphanCheck = persona.getAlumno();
            if (alumnoOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Persona (" + persona + ") cannot be destroyed since the Alumno " + alumnoOrphanCheck + " in its alumno field has a non-nullable persona field.");
            }
            Usuario usuarioOrphanCheck = persona.getUsuario();
            if (usuarioOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Persona (" + persona + ") cannot be destroyed since the Usuario " + usuarioOrphanCheck + " in its usuario field has a non-nullable rutPersona field.");
            }
            Docente docenteOrphanCheck = persona.getDocente();
            if (docenteOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Persona (" + persona + ") cannot be destroyed since the Docente " + docenteOrphanCheck + " in its docente field has a non-nullable persona field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(persona);
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

    public List<Persona> findPersonaEntities() {
        return findPersonaEntities(true, -1, -1);
    }

    public List<Persona> findPersonaEntities(int maxResults, int firstResult) {
        return findPersonaEntities(false, maxResults, firstResult);
    }

    private List<Persona> findPersonaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Persona.class));
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

    public Persona findPersona(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Persona.class, id);
        } finally {
            em.close();
        }
    }

    public int getPersonaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Persona> rt = cq.from(Persona.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
