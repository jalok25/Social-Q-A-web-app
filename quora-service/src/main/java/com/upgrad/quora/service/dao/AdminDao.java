package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class AdminDao {

    @PersistenceContext
    private EntityManager entityManager;

    //Gets the user uuid and delete the user in the database
    public UserEntity deleteUser(String userId) {
        try {
            UserEntity  userEntity = entityManager.createNamedQuery("userByUuid", UserEntity.class).
                    setParameter("uuid", userId).getSingleResult();
            entityManager.remove(userEntity);
            return userEntity;
        }
        catch (NoResultException nre) {
            return null;
        }
    }

    //Get the authorization and return the UserAuthEntity
    public UserAuthEntity getAccessToken(String authorization) {
        try {
            UserAuthEntity userAuthEntity = entityManager.
                    createNamedQuery("userAuthTokenByAccessToken", UserAuthEntity.class).
                    setParameter("accessToken", authorization).getSingleResult();
            return userAuthEntity;
        }
        catch (NoResultException nre) {
            return null;
        }
    }

}
