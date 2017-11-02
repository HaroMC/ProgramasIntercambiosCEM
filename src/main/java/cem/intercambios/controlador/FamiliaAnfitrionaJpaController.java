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
import cem.intercambios.entidades.Persona;
import cem.intercambios.entidades.Antecedente;
import cem.intercambios.entidades.FamiliaAnfitriona;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author cetecom
 */
public class FamiliaAnfitrionaJpaController implements Serializable {

    public FamiliaAnfitrionaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(FamiliaAnfitriona familiaAnfitriona) throws IllegalOrphanException, PreexistingEntityException, RollbackFailureException, Exception {
        if (familiaAnfitriona.getAntecedenteList() == null) {
            familiaAnfitriona.setAntecedenteList(new ArrayList<Antecedente>());
        }
        List<String> illegalOrphanMessages = null;
        Persona personaOrphanCheck = familiaAnfitriona.getPersona();
        if (personaOrphanCheck != null) {
            FamiliaAnfitriona oldFamiliaAnfitrionaOfPersona = personaOrphanCheck.getFamiliaAnfitriona();
            if (oldFamiliaAnfitrionaOfPersona != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("The Persona " + personaOrphanCheck + " already has an item of type FamiliaAnfitriona whose persona column cannot be null. Please make another selection for the persona field.");
            }
        }
        if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Persona persona = familiaAnfitriona.getPersona();
            if (persona != null) {
                persona = em.getReference(persona.getClass(), persona.getRut());
                familiaAnfitriona.setPersona(persona);
            }
            List<Antecedente> attachedAntecedenteList = new ArrayList<Antecedente>();
            for (Antecedente antecedenteListAntecedenteToAttach : familiaAnfitriona.getAntecedenteList()) {
                antecedenteListAntecedenteToAttach = em.getReference(antecedenteListAntecedenteToAttach.getClass(), antecedenteListAntecedenteToAttach.getCodigo());
                attachedAntecedenteList.add(antecedenteListAntecedenteToAttach);
            }
            familiaAnfitriona.setAntecedenteList(attachedAntecedenteList);
            em.persist(familiaAnfitriona);
            if (persona != null) {
                persona.setFamiliaAnfitriona(familiaAnfitriona);
                persona = em.merge(persona);
            }
            for (Antecedente antecedenteListAntecedente : familiaAnfitriona.getAntecedenteList()) {
                FamiliaAnfitriona oldRutFamiliaOfAntecedenteListAntecedente = antecedenteListAntecedente.getRutFamilia();
                antecedenteListAntecedente.setRutFamilia(familiaAnfitriona);
                antecedenteListAntecedente = em.merge(antecedenteListAntecedente);
                if (oldRutFamiliaOfAntecedenteListAntecedente != null) {
                    oldRutFamiliaOfAntecedenteListAntecedente.getAntecedenteList().remove(antecedenteListAntecedente);
                    oldRutFamiliaOfAntecedenteListAntecedente = em.merge(oldRutFamiliaOfAntecedenteListAntecedente);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findFamiliaAnfitriona(familiaAnfitriona.getRutPersona()) != null) {
                throw new PreexistingEntityException("FamiliaAnfitriona " + familiaAnfitriona + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(FamiliaAnfitriona familiaAnfitriona) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            FamiliaAnfitriona persistentFamiliaAnfitriona = em.find(FamiliaAnfitriona.class, familiaAnfitriona.getRutPersona());
            Persona personaOld = persistentFamiliaAnfitriona.getPersona();
            Persona personaNew = familiaAnfitriona.getPersona();
            List<Antecedente> antecedenteListOld = persistentFamiliaAnfitriona.getAntecedenteList();
            List<Antecedente> antecedenteListNew = familiaAnfitriona.getAntecedenteList();
            List<String> illegalOrphanMessages = null;
            if (personaNew != null && !personaNew.equals(personaOld)) {
                FamiliaAnfitriona oldFamiliaAnfitrionaOfPersona = personaNew.getFamiliaAnfitriona();
                if (oldFamiliaAnfitrionaOfPersona != null) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("The Persona " + personaNew + " already has an item of type FamiliaAnfitriona whose persona column cannot be null. Please make another selection for the persona field.");
                }
            }
            for (Antecedente antecedenteListOldAntecedente : antecedenteListOld) {
                if (!antecedenteListNew.contains(antecedenteListOldAntecedente)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Antecedente " + antecedenteListOldAntecedente + " since its rutFamilia field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (personaNew != null) {
                personaNew = em.getReference(personaNew.getClass(), personaNew.getRut());
                familiaAnfitriona.setPersona(personaNew);
            }
            List<Antecedente> attachedAntecedenteListNew = new ArrayList<Antecedente>();
            for (Antecedente antecedenteListNewAntecedenteToAttach : antecedenteListNew) {
                antecedenteListNewAntecedenteToAttach = em.getReference(antecedenteListNewAntecedenteToAttach.getClass(), antecedenteListNewAntecedenteToAttach.getCodigo());
                attachedAntecedenteListNew.add(antecedenteListNewAntecedenteToAttach);
            }
            antecedenteListNew = attachedAntecedenteListNew;
            familiaAnfitriona.setAntecedenteList(antecedenteListNew);
            familiaAnfitriona = em.merge(familiaAnfitriona);
            if (personaOld != null && !personaOld.equals(personaNew)) {
                personaOld.setFamiliaAnfitriona(null);
                personaOld = em.merge(personaOld);
            }
            if (personaNew != null && !personaNew.equals(personaOld)) {
                personaNew.setFamiliaAnfitriona(familiaAnfitriona);
                personaNew = em.merge(personaNew);
            }
            for (Antecedente antecedenteListNewAntecedente : antecedenteListNew) {
                if (!antecedenteListOld.contains(antecedenteListNewAntecedente)) {
                    FamiliaAnfitriona oldRutFamiliaOfAntecedenteListNewAntecedente = antecedenteListNewAntecedente.getRutFamilia();
                    antecedenteListNewAntecedente.setRutFamilia(familiaAnfitriona);
                    antecedenteListNewAntecedente = em.merge(antecedenteListNewAntecedente);
                    if (oldRutFamiliaOfAntecedenteListNewAntecedente != null && !oldRutFamiliaOfAntecedenteListNewAntecedente.equals(familiaAnfitriona)) {
                        oldRutFamiliaOfAntecedenteListNewAntecedente.getAntecedenteList().remove(antecedenteListNewAntecedente);
                        oldRutFamiliaOfAntecedenteListNewAntecedente = em.merge(oldRutFamiliaOfAntecedenteListNewAntecedente);
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
                Integer id = familiaAnfitriona.getRutPersona();
                if (findFamiliaAnfitriona(id) == null) {
                    throw new NonexistentEntityException("The familiaAnfitriona with id " + id + " no longer exists.");
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
            FamiliaAnfitriona familiaAnfitriona;
            try {
                familiaAnfitriona = em.getReference(FamiliaAnfitriona.class, id);
                familiaAnfitriona.getRutPersona();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The familiaAnfitriona with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Antecedente> antecedenteListOrphanCheck = familiaAnfitriona.getAntecedenteList();
            for (Antecedente antecedenteListOrphanCheckAntecedente : antecedenteListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This FamiliaAnfitriona (" + familiaAnfitriona + ") cannot be destroyed since the Antecedente " + antecedenteListOrphanCheckAntecedente + " in its antecedenteList field has a non-nullable rutFamilia field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Persona persona = familiaAnfitriona.getPersona();
            if (persona != null) {
                persona.setFamiliaAnfitriona(null);
                persona = em.merge(persona);
            }
            em.remove(familiaAnfitriona);
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

    public List<FamiliaAnfitriona> findFamiliaAnfitrionaEntities() {
        return findFamiliaAnfitrionaEntities(true, -1, -1);
    }

    public List<FamiliaAnfitriona> findFamiliaAnfitrionaEntities(int maxResults, int firstResult) {
        return findFamiliaAnfitrionaEntities(false, maxResults, firstResult);
    }

    private List<FamiliaAnfitriona> findFamiliaAnfitrionaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(FamiliaAnfitriona.class));
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

    public FamiliaAnfitriona findFamiliaAnfitriona(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(FamiliaAnfitriona.class, id);
        } finally {
            em.close();
        }
    }

    public int getFamiliaAnfitrionaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<FamiliaAnfitriona> rt = cq.from(FamiliaAnfitriona.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
