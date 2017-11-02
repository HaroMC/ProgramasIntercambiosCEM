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
import cem.intercambios.entidades.CentroEstudiosLocal;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import cem.intercambios.entidades.Persona;
import cem.intercambios.entidades.PostulacionesCel;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author cetecom
 */
public class CentroEstudiosLocalJpaController implements Serializable {

    public CentroEstudiosLocalJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(CentroEstudiosLocal centroEstudiosLocal) throws IllegalOrphanException, PreexistingEntityException, RollbackFailureException, Exception {
        if (centroEstudiosLocal.getPostulacionesCelList() == null) {
            centroEstudiosLocal.setPostulacionesCelList(new ArrayList<PostulacionesCel>());
        }
        List<String> illegalOrphanMessages = null;
        Persona personaOrphanCheck = centroEstudiosLocal.getPersona();
        if (personaOrphanCheck != null) {
            CentroEstudiosLocal oldCentroEstudiosLocalOfPersona = personaOrphanCheck.getCentroEstudiosLocal();
            if (oldCentroEstudiosLocalOfPersona != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("The Persona " + personaOrphanCheck + " already has an item of type CentroEstudiosLocal whose persona column cannot be null. Please make another selection for the persona field.");
            }
        }
        if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Persona persona = centroEstudiosLocal.getPersona();
            if (persona != null) {
                persona = em.getReference(persona.getClass(), persona.getRut());
                centroEstudiosLocal.setPersona(persona);
            }
            List<PostulacionesCel> attachedPostulacionesCelList = new ArrayList<PostulacionesCel>();
            for (PostulacionesCel postulacionesCelListPostulacionesCelToAttach : centroEstudiosLocal.getPostulacionesCelList()) {
                postulacionesCelListPostulacionesCelToAttach = em.getReference(postulacionesCelListPostulacionesCelToAttach.getClass(), postulacionesCelListPostulacionesCelToAttach.getCodigo());
                attachedPostulacionesCelList.add(postulacionesCelListPostulacionesCelToAttach);
            }
            centroEstudiosLocal.setPostulacionesCelList(attachedPostulacionesCelList);
            em.persist(centroEstudiosLocal);
            if (persona != null) {
                persona.setCentroEstudiosLocal(centroEstudiosLocal);
                persona = em.merge(persona);
            }
            for (PostulacionesCel postulacionesCelListPostulacionesCel : centroEstudiosLocal.getPostulacionesCelList()) {
                CentroEstudiosLocal oldRutCelOfPostulacionesCelListPostulacionesCel = postulacionesCelListPostulacionesCel.getRutCel();
                postulacionesCelListPostulacionesCel.setRutCel(centroEstudiosLocal);
                postulacionesCelListPostulacionesCel = em.merge(postulacionesCelListPostulacionesCel);
                if (oldRutCelOfPostulacionesCelListPostulacionesCel != null) {
                    oldRutCelOfPostulacionesCelListPostulacionesCel.getPostulacionesCelList().remove(postulacionesCelListPostulacionesCel);
                    oldRutCelOfPostulacionesCelListPostulacionesCel = em.merge(oldRutCelOfPostulacionesCelListPostulacionesCel);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findCentroEstudiosLocal(centroEstudiosLocal.getRutPersona()) != null) {
                throw new PreexistingEntityException("CentroEstudiosLocal " + centroEstudiosLocal + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(CentroEstudiosLocal centroEstudiosLocal) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            CentroEstudiosLocal persistentCentroEstudiosLocal = em.find(CentroEstudiosLocal.class, centroEstudiosLocal.getRutPersona());
            Persona personaOld = persistentCentroEstudiosLocal.getPersona();
            Persona personaNew = centroEstudiosLocal.getPersona();
            List<PostulacionesCel> postulacionesCelListOld = persistentCentroEstudiosLocal.getPostulacionesCelList();
            List<PostulacionesCel> postulacionesCelListNew = centroEstudiosLocal.getPostulacionesCelList();
            List<String> illegalOrphanMessages = null;
            if (personaNew != null && !personaNew.equals(personaOld)) {
                CentroEstudiosLocal oldCentroEstudiosLocalOfPersona = personaNew.getCentroEstudiosLocal();
                if (oldCentroEstudiosLocalOfPersona != null) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("The Persona " + personaNew + " already has an item of type CentroEstudiosLocal whose persona column cannot be null. Please make another selection for the persona field.");
                }
            }
            for (PostulacionesCel postulacionesCelListOldPostulacionesCel : postulacionesCelListOld) {
                if (!postulacionesCelListNew.contains(postulacionesCelListOldPostulacionesCel)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain PostulacionesCel " + postulacionesCelListOldPostulacionesCel + " since its rutCel field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (personaNew != null) {
                personaNew = em.getReference(personaNew.getClass(), personaNew.getRut());
                centroEstudiosLocal.setPersona(personaNew);
            }
            List<PostulacionesCel> attachedPostulacionesCelListNew = new ArrayList<PostulacionesCel>();
            for (PostulacionesCel postulacionesCelListNewPostulacionesCelToAttach : postulacionesCelListNew) {
                postulacionesCelListNewPostulacionesCelToAttach = em.getReference(postulacionesCelListNewPostulacionesCelToAttach.getClass(), postulacionesCelListNewPostulacionesCelToAttach.getCodigo());
                attachedPostulacionesCelListNew.add(postulacionesCelListNewPostulacionesCelToAttach);
            }
            postulacionesCelListNew = attachedPostulacionesCelListNew;
            centroEstudiosLocal.setPostulacionesCelList(postulacionesCelListNew);
            centroEstudiosLocal = em.merge(centroEstudiosLocal);
            if (personaOld != null && !personaOld.equals(personaNew)) {
                personaOld.setCentroEstudiosLocal(null);
                personaOld = em.merge(personaOld);
            }
            if (personaNew != null && !personaNew.equals(personaOld)) {
                personaNew.setCentroEstudiosLocal(centroEstudiosLocal);
                personaNew = em.merge(personaNew);
            }
            for (PostulacionesCel postulacionesCelListNewPostulacionesCel : postulacionesCelListNew) {
                if (!postulacionesCelListOld.contains(postulacionesCelListNewPostulacionesCel)) {
                    CentroEstudiosLocal oldRutCelOfPostulacionesCelListNewPostulacionesCel = postulacionesCelListNewPostulacionesCel.getRutCel();
                    postulacionesCelListNewPostulacionesCel.setRutCel(centroEstudiosLocal);
                    postulacionesCelListNewPostulacionesCel = em.merge(postulacionesCelListNewPostulacionesCel);
                    if (oldRutCelOfPostulacionesCelListNewPostulacionesCel != null && !oldRutCelOfPostulacionesCelListNewPostulacionesCel.equals(centroEstudiosLocal)) {
                        oldRutCelOfPostulacionesCelListNewPostulacionesCel.getPostulacionesCelList().remove(postulacionesCelListNewPostulacionesCel);
                        oldRutCelOfPostulacionesCelListNewPostulacionesCel = em.merge(oldRutCelOfPostulacionesCelListNewPostulacionesCel);
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
                Integer id = centroEstudiosLocal.getRutPersona();
                if (findCentroEstudiosLocal(id) == null) {
                    throw new NonexistentEntityException("The centroEstudiosLocal with id " + id + " no longer exists.");
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
            CentroEstudiosLocal centroEstudiosLocal;
            try {
                centroEstudiosLocal = em.getReference(CentroEstudiosLocal.class, id);
                centroEstudiosLocal.getRutPersona();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The centroEstudiosLocal with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<PostulacionesCel> postulacionesCelListOrphanCheck = centroEstudiosLocal.getPostulacionesCelList();
            for (PostulacionesCel postulacionesCelListOrphanCheckPostulacionesCel : postulacionesCelListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This CentroEstudiosLocal (" + centroEstudiosLocal + ") cannot be destroyed since the PostulacionesCel " + postulacionesCelListOrphanCheckPostulacionesCel + " in its postulacionesCelList field has a non-nullable rutCel field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Persona persona = centroEstudiosLocal.getPersona();
            if (persona != null) {
                persona.setCentroEstudiosLocal(null);
                persona = em.merge(persona);
            }
            em.remove(centroEstudiosLocal);
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

    public List<CentroEstudiosLocal> findCentroEstudiosLocalEntities() {
        return findCentroEstudiosLocalEntities(true, -1, -1);
    }

    public List<CentroEstudiosLocal> findCentroEstudiosLocalEntities(int maxResults, int firstResult) {
        return findCentroEstudiosLocalEntities(false, maxResults, firstResult);
    }

    private List<CentroEstudiosLocal> findCentroEstudiosLocalEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(CentroEstudiosLocal.class));
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

    public CentroEstudiosLocal findCentroEstudiosLocal(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(CentroEstudiosLocal.class, id);
        } finally {
            em.close();
        }
    }

    public int getCentroEstudiosLocalCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<CentroEstudiosLocal> rt = cq.from(CentroEstudiosLocal.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
