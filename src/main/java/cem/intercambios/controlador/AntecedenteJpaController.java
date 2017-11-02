/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cem.intercambios.controlador;

import cem.intercambios.controlador.exceptions.NonexistentEntityException;
import cem.intercambios.controlador.exceptions.PreexistingEntityException;
import cem.intercambios.controlador.exceptions.RollbackFailureException;
import cem.intercambios.entidades.Antecedente;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import cem.intercambios.entidades.FamiliaAnfitriona;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author cetecom
 */
public class AntecedenteJpaController implements Serializable {

    public AntecedenteJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Antecedente antecedente) throws PreexistingEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            FamiliaAnfitriona rutFamilia = antecedente.getRutFamilia();
            if (rutFamilia != null) {
                rutFamilia = em.getReference(rutFamilia.getClass(), rutFamilia.getRutPersona());
                antecedente.setRutFamilia(rutFamilia);
            }
            em.persist(antecedente);
            if (rutFamilia != null) {
                rutFamilia.getAntecedenteList().add(antecedente);
                rutFamilia = em.merge(rutFamilia);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findAntecedente(antecedente.getCodigo()) != null) {
                throw new PreexistingEntityException("Antecedente " + antecedente + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Antecedente antecedente) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Antecedente persistentAntecedente = em.find(Antecedente.class, antecedente.getCodigo());
            FamiliaAnfitriona rutFamiliaOld = persistentAntecedente.getRutFamilia();
            FamiliaAnfitriona rutFamiliaNew = antecedente.getRutFamilia();
            if (rutFamiliaNew != null) {
                rutFamiliaNew = em.getReference(rutFamiliaNew.getClass(), rutFamiliaNew.getRutPersona());
                antecedente.setRutFamilia(rutFamiliaNew);
            }
            antecedente = em.merge(antecedente);
            if (rutFamiliaOld != null && !rutFamiliaOld.equals(rutFamiliaNew)) {
                rutFamiliaOld.getAntecedenteList().remove(antecedente);
                rutFamiliaOld = em.merge(rutFamiliaOld);
            }
            if (rutFamiliaNew != null && !rutFamiliaNew.equals(rutFamiliaOld)) {
                rutFamiliaNew.getAntecedenteList().add(antecedente);
                rutFamiliaNew = em.merge(rutFamiliaNew);
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
                BigDecimal id = antecedente.getCodigo();
                if (findAntecedente(id) == null) {
                    throw new NonexistentEntityException("The antecedente with id " + id + " no longer exists.");
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
            Antecedente antecedente;
            try {
                antecedente = em.getReference(Antecedente.class, id);
                antecedente.getCodigo();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The antecedente with id " + id + " no longer exists.", enfe);
            }
            FamiliaAnfitriona rutFamilia = antecedente.getRutFamilia();
            if (rutFamilia != null) {
                rutFamilia.getAntecedenteList().remove(antecedente);
                rutFamilia = em.merge(rutFamilia);
            }
            em.remove(antecedente);
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

    public List<Antecedente> findAntecedenteEntities() {
        return findAntecedenteEntities(true, -1, -1);
    }

    public List<Antecedente> findAntecedenteEntities(int maxResults, int firstResult) {
        return findAntecedenteEntities(false, maxResults, firstResult);
    }

    private List<Antecedente> findAntecedenteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Antecedente.class));
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

    public Antecedente findAntecedente(BigDecimal id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Antecedente.class, id);
        } finally {
            em.close();
        }
    }

    public int getAntecedenteCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Antecedente> rt = cq.from(Antecedente.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
