package com.orange.malimacollector.validator;

import com.orange.malimacollector.dao.AppUserDAO;
import com.orange.malimacollector.entities.login.AppUser;
import com.orange.malimacollector.model.AppUserForm;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class AppUserValidator implements Validator {

    private static final String USER = "userName";
    private static final String EMAIL = "email";

    private EmailValidator emailValidator = EmailValidator.getInstance();

    @Autowired
    private AppUserDAO appUserDAO;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == AppUserForm.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        AppUserForm appUserForm = (AppUserForm) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, USER, "NotEmpty.appUserForm.userName");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, EMAIL, "NotEmpty.appUserForm.email");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty.appUserForm.password");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confirmPassword", "NotEmpty.appUserForm.confirmPassword");

        if (!this.emailValidator.isValid(appUserForm.getEmail())) {
            errors.rejectValue(EMAIL, "Pattern.appUserForm.email");
        } else if (appUserForm.getUserId() == null) {
            AppUser dbUser = appUserDAO.findAppUserByEmail(appUserForm.getEmail());
            if (dbUser != null) {
                errors.rejectValue(EMAIL, "Duplicate.appUserForm.email");
            }
        }

        if (!errors.hasFieldErrors(USER)) {
            AppUser dbUser = appUserDAO.findAppUserByUserName(appUserForm.getUserName());
            if (dbUser != null) {
                errors.rejectValue(USER, "Duplicate.appUserForm.userName");
            }
        }

        if (!errors.hasErrors() && !appUserForm.getConfirmPassword().equals(appUserForm.getPassword())) {
            errors.rejectValue("confirmPassword", "Match.appUserForm.confirmPassword");
        }
    }
}