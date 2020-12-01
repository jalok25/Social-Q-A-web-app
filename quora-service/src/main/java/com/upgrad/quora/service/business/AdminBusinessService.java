package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AdminDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class AdminBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private AdminDao adminDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity createUser(final UserEntity userEntity) throws SignUpRestrictedException {

        // Validate if requested user name is available
        UserEntity existingUser = userDao.getUserByUserName(userEntity.getUserName());
        if (existingUser != null) {
            throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");
        }

        // Validate if provided email id is available
        existingUser = userDao.getUserByEmail(userEntity.getEmail());
        if (existingUser != null) {
            throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other emailID");
        }

        // Assign default password if password is not provided
        String password = userEntity.getPassword();
        if (password == null) {
            userEntity.setPassword("quora-123");
        }

        // Encrypt salt and password
        String[] encryptedText = cryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);

        return userDao.createUser(userEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity logoutUser(final String authorizationToken) throws SignOutRestrictedException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorizationToken);

        // Validate if user is signed in or not
        if (userAuthEntity == null) {
            throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
        }

        final ZonedDateTime now = ZonedDateTime.now();
        userAuthEntity.setLogoutAt(now);

        return userAuthEntity.getUser();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity deleteUser(String userID, String authorization) throws UserNotFoundException,
            AuthorizationFailedException{
        UserAuthEntity userAuthEntity = adminDao.getAccessToken(authorization);
        if(userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        else if(userAuthEntity.getLogoutAt()!=null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out");
        }

        boolean isAdmin = userAuthEntity.getUser().getRole().equals("admin");

        if(isAdmin) {
            UserEntity userEntity = adminDao.deleteUser(userID);
            if(userEntity != null) {
                return userEntity;
            }
            else {
                throw new UserNotFoundException("USR-001", "User with entered uuid to be deleted does not exist");
            }
        }
        else {
            throw  new AuthorizationFailedException("ATHR-003","Unauthorized Access, Entered user is not an admin");
        }
    }

}
