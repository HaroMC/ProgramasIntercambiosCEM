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
import cem.intercambios.entidades.Certificado;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author cetecom
 */
public class CertificadoJpaController implements Serializable {

    public CertificadoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Certificado certificado) throws PreexistingEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Alumno rutAlumno = certificado.getRutAlumno();
            if (rutAlumno != null) {
                rutAlumno = em.getReference(rutAlumno.getClass(), rutAlumno.getRutPersona());
                certificado.setRutAlumno(rutAlumno);
            }
            em.persist(certificado);
            if (rutAlumno != null) {
                rutAlumno.getCertificadoList().add(certificado);
                rutAlumno = em.merge(rutAlumno);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findCertificado(certificado.getCodigo()) != null) {
                throw new PreexistingEntityException("Certificado " + certificado + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Certificado certificado) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Certificado persistentCertificado = em.find(Certificado.class, certificado.getCodigo());
            Alumno rutAlumnoOld = persistentCertificado.getRutAlumno();
            Alumno rutAlumnoNew = certificado.getRutAlumno();
            if (rutAlumnoNew != null) {
                rutAlumnoNew = em.getReference(rutAlumnoNew.getClass(), rutAlumnoNew.getRutPersona());
                certificado.setRutAlumno(rutAlumnoNew);
            }
            certificado = em.merge(certificado);
            if (rutAlumnoOld != null && !rutAlumnoOld.equals(rutAlumnoNew)) {
                rutAlumnoOld.getCertificadoList().remove(certificado);
                rutAlumnoOld = em.merge(rutAlumnoOld);
            }
            if (rutAlumnoNew != null && !rutAlumnoNew.equals(rutAlumnoOld)) {
                rutAlumnoNew.getCertificadoList().add(certificado);
                rutAlumnoNew = em.merge(rutAlumnoNew);
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
                BigDecimal id = certificado.getCodigo();
                if (findCertificado(id) == null) {
                    throw new NonexistentEntityException("The certificado with id " + id + " no longer exists.");
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
            Certificado certificado;
            try {
                certificado = em.getReference(Certificado.class, id);
                certificado.getCodigo();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The certificado with id " + id + " no longer exists.", enfe);
            }
            Alumno rutAlumno = certificado.getRutAlumno();
            if (rutAlumno != null) {
                rutAlumno.getCertificadoList().remove(certificado);
                rutAlumno = em.merge(rutAlumno);
            }
            em.remove(certificado);
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

    public List<Certificado> findCertificadoEntities() {
        return findCertificadoEntities(true, -1, -1);
    }

    public List<Certificado> findCertificadoEntities(int maxResults, int firstResult) {
        return findCertificadoEntities(false, maxResults, firstResult);
    }

    private List<Certificado> findCertificadoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Certificado.class));
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

    public Certificado findCertificado(BigDecimal id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Certificado.class, id);
        } finally {
            em.close();
        }
    }

    public int getCertificadoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Certificado> rt = cq.from(Certificado.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
