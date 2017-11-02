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
import cem.intercambios.entidades.Alumno;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import cem.intercambios.entidades.Persona;
import cem.intercambios.entidades.PostulacionesAlumnos;
import java.util.ArrayList;
import java.util.List;
import cem.intercambios.entidades.Calificacion;
import cem.intercambios.entidades.Certificado;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author cetecom
 */
public class AlumnoJpaController implements Serializable {

    public AlumnoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Alumno alumno) throws IllegalOrphanException, PreexistingEntityException, RollbackFailureException, Exception {
        if (alumno.getPostulacionesAlumnosList() == null) {
            alumno.setPostulacionesAlumnosList(new ArrayList<PostulacionesAlumnos>());
        }
        if (alumno.getCalificacionList() == null) {
            alumno.setCalificacionList(new ArrayList<Calificacion>());
        }
        if (alumno.getCertificadoList() == null) {
            alumno.setCertificadoList(new ArrayList<Certificado>());
        }
        List<String> illegalOrphanMessages = null;
        Persona personaOrphanCheck = alumno.getPersona();
        if (personaOrphanCheck != null) {
            Alumno oldAlumnoOfPersona = personaOrphanCheck.getAlumno();
            if (oldAlumnoOfPersona != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("The Persona " + personaOrphanCheck + " already has an item of type Alumno whose persona column cannot be null. Please make another selection for the persona field.");
            }
        }
        if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Persona persona = alumno.getPersona();
            if (persona != null) {
                persona = em.getReference(persona.getClass(), persona.getRut());
                alumno.setPersona(persona);
            }
            List<PostulacionesAlumnos> attachedPostulacionesAlumnosList = new ArrayList<PostulacionesAlumnos>();
            for (PostulacionesAlumnos postulacionesAlumnosListPostulacionesAlumnosToAttach : alumno.getPostulacionesAlumnosList()) {
                postulacionesAlumnosListPostulacionesAlumnosToAttach = em.getReference(postulacionesAlumnosListPostulacionesAlumnosToAttach.getClass(), postulacionesAlumnosListPostulacionesAlumnosToAttach.getCodigo());
                attachedPostulacionesAlumnosList.add(postulacionesAlumnosListPostulacionesAlumnosToAttach);
            }
            alumno.setPostulacionesAlumnosList(attachedPostulacionesAlumnosList);
            List<Calificacion> attachedCalificacionList = new ArrayList<Calificacion>();
            for (Calificacion calificacionListCalificacionToAttach : alumno.getCalificacionList()) {
                calificacionListCalificacionToAttach = em.getReference(calificacionListCalificacionToAttach.getClass(), calificacionListCalificacionToAttach.getCodigo());
                attachedCalificacionList.add(calificacionListCalificacionToAttach);
            }
            alumno.setCalificacionList(attachedCalificacionList);
            List<Certificado> attachedCertificadoList = new ArrayList<Certificado>();
            for (Certificado certificadoListCertificadoToAttach : alumno.getCertificadoList()) {
                certificadoListCertificadoToAttach = em.getReference(certificadoListCertificadoToAttach.getClass(), certificadoListCertificadoToAttach.getCodigo());
                attachedCertificadoList.add(certificadoListCertificadoToAttach);
            }
            alumno.setCertificadoList(attachedCertificadoList);
            em.persist(alumno);
            if (persona != null) {
                persona.setAlumno(alumno);
                persona = em.merge(persona);
            }
            for (PostulacionesAlumnos postulacionesAlumnosListPostulacionesAlumnos : alumno.getPostulacionesAlumnosList()) {
                Alumno oldRutAlumnoOfPostulacionesAlumnosListPostulacionesAlumnos = postulacionesAlumnosListPostulacionesAlumnos.getRutAlumno();
                postulacionesAlumnosListPostulacionesAlumnos.setRutAlumno(alumno);
                postulacionesAlumnosListPostulacionesAlumnos = em.merge(postulacionesAlumnosListPostulacionesAlumnos);
                if (oldRutAlumnoOfPostulacionesAlumnosListPostulacionesAlumnos != null) {
                    oldRutAlumnoOfPostulacionesAlumnosListPostulacionesAlumnos.getPostulacionesAlumnosList().remove(postulacionesAlumnosListPostulacionesAlumnos);
                    oldRutAlumnoOfPostulacionesAlumnosListPostulacionesAlumnos = em.merge(oldRutAlumnoOfPostulacionesAlumnosListPostulacionesAlumnos);
                }
            }
            for (Calificacion calificacionListCalificacion : alumno.getCalificacionList()) {
                Alumno oldRutAlumnoOfCalificacionListCalificacion = calificacionListCalificacion.getRutAlumno();
                calificacionListCalificacion.setRutAlumno(alumno);
                calificacionListCalificacion = em.merge(calificacionListCalificacion);
                if (oldRutAlumnoOfCalificacionListCalificacion != null) {
                    oldRutAlumnoOfCalificacionListCalificacion.getCalificacionList().remove(calificacionListCalificacion);
                    oldRutAlumnoOfCalificacionListCalificacion = em.merge(oldRutAlumnoOfCalificacionListCalificacion);
                }
            }
            for (Certificado certificadoListCertificado : alumno.getCertificadoList()) {
                Alumno oldRutAlumnoOfCertificadoListCertificado = certificadoListCertificado.getRutAlumno();
                certificadoListCertificado.setRutAlumno(alumno);
                certificadoListCertificado = em.merge(certificadoListCertificado);
                if (oldRutAlumnoOfCertificadoListCertificado != null) {
                    oldRutAlumnoOfCertificadoListCertificado.getCertificadoList().remove(certificadoListCertificado);
                    oldRutAlumnoOfCertificadoListCertificado = em.merge(oldRutAlumnoOfCertificadoListCertificado);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findAlumno(alumno.getRutPersona()) != null) {
                throw new PreexistingEntityException("Alumno " + alumno + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Alumno alumno) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Alumno persistentAlumno = em.find(Alumno.class, alumno.getRutPersona());
            Persona personaOld = persistentAlumno.getPersona();
            Persona personaNew = alumno.getPersona();
            List<PostulacionesAlumnos> postulacionesAlumnosListOld = persistentAlumno.getPostulacionesAlumnosList();
            List<PostulacionesAlumnos> postulacionesAlumnosListNew = alumno.getPostulacionesAlumnosList();
            List<Calificacion> calificacionListOld = persistentAlumno.getCalificacionList();
            List<Calificacion> calificacionListNew = alumno.getCalificacionList();
            List<Certificado> certificadoListOld = persistentAlumno.getCertificadoList();
            List<Certificado> certificadoListNew = alumno.getCertificadoList();
            List<String> illegalOrphanMessages = null;
            if (personaNew != null && !personaNew.equals(personaOld)) {
                Alumno oldAlumnoOfPersona = personaNew.getAlumno();
                if (oldAlumnoOfPersona != null) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("The Persona " + personaNew + " already has an item of type Alumno whose persona column cannot be null. Please make another selection for the persona field.");
                }
            }
            for (PostulacionesAlumnos postulacionesAlumnosListOldPostulacionesAlumnos : postulacionesAlumnosListOld) {
                if (!postulacionesAlumnosListNew.contains(postulacionesAlumnosListOldPostulacionesAlumnos)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain PostulacionesAlumnos " + postulacionesAlumnosListOldPostulacionesAlumnos + " since its rutAlumno field is not nullable.");
                }
            }
            for (Calificacion calificacionListOldCalificacion : calificacionListOld) {
                if (!calificacionListNew.contains(calificacionListOldCalificacion)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Calificacion " + calificacionListOldCalificacion + " since its rutAlumno field is not nullable.");
                }
            }
            for (Certificado certificadoListOldCertificado : certificadoListOld) {
                if (!certificadoListNew.contains(certificadoListOldCertificado)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Certificado " + certificadoListOldCertificado + " since its rutAlumno field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (personaNew != null) {
                personaNew = em.getReference(personaNew.getClass(), personaNew.getRut());
                alumno.setPersona(personaNew);
            }
            List<PostulacionesAlumnos> attachedPostulacionesAlumnosListNew = new ArrayList<PostulacionesAlumnos>();
            for (PostulacionesAlumnos postulacionesAlumnosListNewPostulacionesAlumnosToAttach : postulacionesAlumnosListNew) {
                postulacionesAlumnosListNewPostulacionesAlumnosToAttach = em.getReference(postulacionesAlumnosListNewPostulacionesAlumnosToAttach.getClass(), postulacionesAlumnosListNewPostulacionesAlumnosToAttach.getCodigo());
                attachedPostulacionesAlumnosListNew.add(postulacionesAlumnosListNewPostulacionesAlumnosToAttach);
            }
            postulacionesAlumnosListNew = attachedPostulacionesAlumnosListNew;
            alumno.setPostulacionesAlumnosList(postulacionesAlumnosListNew);
            List<Calificacion> attachedCalificacionListNew = new ArrayList<Calificacion>();
            for (Calificacion calificacionListNewCalificacionToAttach : calificacionListNew) {
                calificacionListNewCalificacionToAttach = em.getReference(calificacionListNewCalificacionToAttach.getClass(), calificacionListNewCalificacionToAttach.getCodigo());
                attachedCalificacionListNew.add(calificacionListNewCalificacionToAttach);
            }
            calificacionListNew = attachedCalificacionListNew;
            alumno.setCalificacionList(calificacionListNew);
            List<Certificado> attachedCertificadoListNew = new ArrayList<Certificado>();
            for (Certificado certificadoListNewCertificadoToAttach : certificadoListNew) {
                certificadoListNewCertificadoToAttach = em.getReference(certificadoListNewCertificadoToAttach.getClass(), certificadoListNewCertificadoToAttach.getCodigo());
                attachedCertificadoListNew.add(certificadoListNewCertificadoToAttach);
            }
            certificadoListNew = attachedCertificadoListNew;
            alumno.setCertificadoList(certificadoListNew);
            alumno = em.merge(alumno);
            if (personaOld != null && !personaOld.equals(personaNew)) {
                personaOld.setAlumno(null);
                personaOld = em.merge(personaOld);
            }
            if (personaNew != null && !personaNew.equals(personaOld)) {
                personaNew.setAlumno(alumno);
                personaNew = em.merge(personaNew);
            }
            for (PostulacionesAlumnos postulacionesAlumnosListNewPostulacionesAlumnos : postulacionesAlumnosListNew) {
                if (!postulacionesAlumnosListOld.contains(postulacionesAlumnosListNewPostulacionesAlumnos)) {
                    Alumno oldRutAlumnoOfPostulacionesAlumnosListNewPostulacionesAlumnos = postulacionesAlumnosListNewPostulacionesAlumnos.getRutAlumno();
                    postulacionesAlumnosListNewPostulacionesAlumnos.setRutAlumno(alumno);
                    postulacionesAlumnosListNewPostulacionesAlumnos = em.merge(postulacionesAlumnosListNewPostulacionesAlumnos);
                    if (oldRutAlumnoOfPostulacionesAlumnosListNewPostulacionesAlumnos != null && !oldRutAlumnoOfPostulacionesAlumnosListNewPostulacionesAlumnos.equals(alumno)) {
                        oldRutAlumnoOfPostulacionesAlumnosListNewPostulacionesAlumnos.getPostulacionesAlumnosList().remove(postulacionesAlumnosListNewPostulacionesAlumnos);
                        oldRutAlumnoOfPostulacionesAlumnosListNewPostulacionesAlumnos = em.merge(oldRutAlumnoOfPostulacionesAlumnosListNewPostulacionesAlumnos);
                    }
                }
            }
            for (Calificacion calificacionListNewCalificacion : calificacionListNew) {
                if (!calificacionListOld.contains(calificacionListNewCalificacion)) {
                    Alumno oldRutAlumnoOfCalificacionListNewCalificacion = calificacionListNewCalificacion.getRutAlumno();
                    calificacionListNewCalificacion.setRutAlumno(alumno);
                    calificacionListNewCalificacion = em.merge(calificacionListNewCalificacion);
                    if (oldRutAlumnoOfCalificacionListNewCalificacion != null && !oldRutAlumnoOfCalificacionListNewCalificacion.equals(alumno)) {
                        oldRutAlumnoOfCalificacionListNewCalificacion.getCalificacionList().remove(calificacionListNewCalificacion);
                        oldRutAlumnoOfCalificacionListNewCalificacion = em.merge(oldRutAlumnoOfCalificacionListNewCalificacion);
                    }
                }
            }
            for (Certificado certificadoListNewCertificado : certificadoListNew) {
                if (!certificadoListOld.contains(certificadoListNewCertificado)) {
                    Alumno oldRutAlumnoOfCertificadoListNewCertificado = certificadoListNewCertificado.getRutAlumno();
                    certificadoListNewCertificado.setRutAlumno(alumno);
                    certificadoListNewCertificado = em.merge(certificadoListNewCertificado);
                    if (oldRutAlumnoOfCertificadoListNewCertificado != null && !oldRutAlumnoOfCertificadoListNewCertificado.equals(alumno)) {
                        oldRutAlumnoOfCertificadoListNewCertificado.getCertificadoList().remove(certificadoListNewCertificado);
                        oldRutAlumnoOfCertificadoListNewCertificado = em.merge(oldRutAlumnoOfCertificadoListNewCertificado);
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
                Integer id = alumno.getRutPersona();
                if (findAlumno(id) == null) {
                    throw new NonexistentEntityException("The alumno with id " + id + " no longer exists.");
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
            Alumno alumno;
            try {
                alumno = em.getReference(Alumno.class, id);
                alumno.getRutPersona();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The alumno with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<PostulacionesAlumnos> postulacionesAlumnosListOrphanCheck = alumno.getPostulacionesAlumnosList();
            for (PostulacionesAlumnos postulacionesAlumnosListOrphanCheckPostulacionesAlumnos : postulacionesAlumnosListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Alumno (" + alumno + ") cannot be destroyed since the PostulacionesAlumnos " + postulacionesAlumnosListOrphanCheckPostulacionesAlumnos + " in its postulacionesAlumnosList field has a non-nullable rutAlumno field.");
            }
            List<Calificacion> calificacionListOrphanCheck = alumno.getCalificacionList();
            for (Calificacion calificacionListOrphanCheckCalificacion : calificacionListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Alumno (" + alumno + ") cannot be destroyed since the Calificacion " + calificacionListOrphanCheckCalificacion + " in its calificacionList field has a non-nullable rutAlumno field.");
            }
            List<Certificado> certificadoListOrphanCheck = alumno.getCertificadoList();
            for (Certificado certificadoListOrphanCheckCertificado : certificadoListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Alumno (" + alumno + ") cannot be destroyed since the Certificado " + certificadoListOrphanCheckCertificado + " in its certificadoList field has a non-nullable rutAlumno field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Persona persona = alumno.getPersona();
            if (persona != null) {
                persona.setAlumno(null);
                persona = em.merge(persona);
            }
            em.remove(alumno);
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

    public List<Alumno> findAlumnoEntities() {
        return findAlumnoEntities(true, -1, -1);
    }

    public List<Alumno> findAlumnoEntities(int maxResults, int firstResult) {
        return findAlumnoEntities(false, maxResults, firstResult);
    }

    private List<Alumno> findAlumnoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Alumno.class));
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

    public Alumno findAlumno(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Alumno.class, id);
        } finally {
            em.close();
        }
    }

    public int getAlumnoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Alumno> rt = cq.from(Alumno.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
