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
import cem.intercambios.entidades.CentroEstudiosLocal;
import cem.intercambios.entidades.PostulacionesCel;
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
public class PostulacionesCelJpaController implements Serializable {

    public PostulacionesCelJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(PostulacionesCel postulacionesCel) throws PreexistingEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            CentroEstudiosLocal rutCel = postulacionesCel.getRutCel();
            if (rutCel != null) {
                rutCel = em.getReference(rutCel.getClass(), rutCel.getRutPersona());
                postulacionesCel.setRutCel(rutCel);
            }
            Programa codPrograma = postulacionesCel.getCodPrograma();
            if (codPrograma != null) {
                codPrograma = em.getReference(codPrograma.getClass(), codPrograma.getCodigo());
                postulacionesCel.setCodPrograma(codPrograma);
            }
            em.persist(postulacionesCel);
            if (rutCel != null) {
                rutCel.getPostulacionesCelList().add(postulacionesCel);
                rutCel = em.merge(rutCel);
            }
            if (codPrograma != null) {
                codPrograma.getPostulacionesCelList().add(postulacionesCel);
                codPrograma = em.merge(codPrograma);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findPostulacionesCel(postulacionesCel.getCodigo()) != null) {
                throw new PreexistingEntityException("PostulacionesCel " + postulacionesCel + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(PostulacionesCel postulacionesCel) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            PostulacionesCel persistentPostulacionesCel = em.find(PostulacionesCel.class, postulacionesCel.getCodigo());
            CentroEstudiosLocal rutCelOld = persistentPostulacionesCel.getRutCel();
            CentroEstudiosLocal rutCelNew = postulacionesCel.getRutCel();
            Programa codProgramaOld = persistentPostulacionesCel.getCodPrograma();
            Programa codProgramaNew = postulacionesCel.getCodPrograma();
            if (rutCelNew != null) {
                rutCelNew = em.getReference(rutCelNew.getClass(), rutCelNew.getRutPersona());
                postulacionesCel.setRutCel(rutCelNew);
            }
            if (codProgramaNew != null) {
                codProgramaNew = em.getReference(codProgramaNew.getClass(), codProgramaNew.getCodigo());
                postulacionesCel.setCodPrograma(codProgramaNew);
            }
            postulacionesCel = em.merge(postulacionesCel);
            if (rutCelOld != null && !rutCelOld.equals(rutCelNew)) {
                rutCelOld.getPostulacionesCelList().remove(postulacionesCel);
                rutCelOld = em.merge(rutCelOld);
            }
            if (rutCelNew != null && !rutCelNew.equals(rutCelOld)) {
                rutCelNew.getPostulacionesCelList().add(postulacionesCel);
                rutCelNew = em.merge(rutCelNew);
            }
            if (codProgramaOld != null && !codProgramaOld.equals(codProgramaNew)) {
                codProgramaOld.getPostulacionesCelList().remove(postulacionesCel);
                codProgramaOld = em.merge(codProgramaOld);
            }
            if (codProgramaNew != null && !codProgramaNew.equals(codProgramaOld)) {
                codProgramaNew.getPostulacionesCelList().add(postulacionesCel);
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
                BigDecimal id = postulacionesCel.getCodigo();
                if (findPostulacionesCel(id) == null) {
                    throw new NonexistentEntityException("The postulacionesCel with id " + id + " no longer exists.");
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
            PostulacionesCel postulacionesCel;
            try {
                postulacionesCel = em.getReference(PostulacionesCel.class, id);
                postulacionesCel.getCodigo();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The postulacionesCel with id " + id + " no longer exists.", enfe);
            }
            CentroEstudiosLocal rutCel = postulacionesCel.getRutCel();
            if (rutCel != null) {
                rutCel.getPostulacionesCelList().remove(postulacionesCel);
                rutCel = em.merge(rutCel);
            }
            Programa codPrograma = postulacionesCel.getCodPrograma();
            if (codPrograma != null) {
                codPrograma.getPostulacionesCelList().remove(postulacionesCel);
                codPrograma = em.merge(codPrograma);
            }
            em.remove(postulacionesCel);
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

    public List<PostulacionesCel> findPostulacionesCelEntities() {
        return findPostulacionesCelEntities(true, -1, -1);
    }

    public List<PostulacionesCel> findPostulacionesCelEntities(int maxResults, int firstResult) {
        return findPostulacionesCelEntities(false, maxResults, firstResult);
    }

    private List<PostulacionesCel> findPostulacionesCelEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(PostulacionesCel.class));
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

    public PostulacionesCel findPostulacionesCel(BigDecimal id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(PostulacionesCel.class, id);
        } finally {
            em.close();
        }
    }

    public int getPostulacionesCelCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<PostulacionesCel> rt = cq.from(PostulacionesCel.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
