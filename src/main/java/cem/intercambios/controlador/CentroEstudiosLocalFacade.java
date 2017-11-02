/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cem.intercambios.controlador;

import cem.intercambios.entidades.CentroEstudiosLocal;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author cetecom
 */
@Stateless
public class CentroEstudiosLocalFacade extends AbstractFacade<CentroEstudiosLocal> {

    @PersistenceContext(unitName = "cem.intercambios_ProgramasIntercambios_war_1.0PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CentroEstudiosLocalFacade() {
        super(CentroEstudiosLocal.class);
    }
    
}
