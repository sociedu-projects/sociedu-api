package com.unishare.api.config;

import com.unishare.api.common.constants.MentorVerificationStatuses;
import com.unishare.api.common.constants.Roles;
import com.unishare.api.common.constants.UserStatuses;
import com.unishare.api.modules.auth.entity.Role;
import com.unishare.api.modules.auth.entity.User;
import com.unishare.api.modules.auth.entity.UserCredential;
import com.unishare.api.modules.auth.entity.UserRole;
import com.unishare.api.modules.auth.repository.RoleRepository;
import com.unishare.api.modules.auth.repository.UserRepository;
import com.unishare.api.modules.service.entity.MentorProfile;
import com.unishare.api.modules.service.entity.PackageCurriculum;
import com.unishare.api.modules.service.entity.ServicePackage;
import com.unishare.api.modules.service.entity.ServicePackageVersion;
import com.unishare.api.modules.service.repository.MentorProfileRepository;
import com.unishare.api.modules.service.repository.PackageCurriculumRepository;
import com.unishare.api.modules.service.repository.ServicePackageRepository;
import com.unishare.api.modules.service.repository.ServicePackageVersionRepository;
import com.unishare.api.modules.user.entity.UserProfile;
import com.unishare.api.modules.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@Profile("local")
@RequiredArgsConstructor
public class LocalDevDataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final ServicePackageVersionRepository servicePackageVersionRepository;
    private final PackageCurriculumRepository packageCurriculumRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner seedLocalData() {
        return args -> {
            Role userRole = ensureRole(Roles.USER);
            Role mentorRole = ensureRole(Roles.MENTOR);
            ensureRole(Roles.ADMIN);

            User mentor = ensureUser(
                    "mentor.local@sociedu.test",
                    "Mentor",
                    "Demo",
                    "Password123!",
                    true,
                    List.of(userRole, mentorRole));

            User mentee = ensureUser(
                    "mentee.local@sociedu.test",
                    "Mentee",
                    "Demo",
                    "Password123!",
                    false,
                    List.of(userRole));

            ensureMentorCatalog(mentor);
            ensureBasicProfile(mentee, "Mentee", "Demo");
        };
    }

    private Role ensureRole(String name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            return roleRepository.save(role);
        });
    }

    private User ensureUser(
            String email,
            String firstName,
            String lastName,
            String rawPassword,
            boolean mentor,
            List<Role> roles) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User created = new User();
            created.setEmail(email);
            created.setEmailVerified(true);
            created.setStatus(UserStatuses.ACTIVE);

            UserCredential credential = new UserCredential();
            credential.setPasswordHash(passwordEncoder.encode(rawPassword));
            created.setCredential(credential);

            for (Role role : roles) {
                UserRole userRole = new UserRole();
                userRole.setRole(role);
                userRole.getId().setRoleId(role.getId());
                created.addUserRole(userRole);
            }

            return userRepository.save(created);
        });

        user.setEmailVerified(true);
        user.setStatus(UserStatuses.ACTIVE);
        userRepository.save(user);

        ensureBasicProfile(user, firstName, lastName);

        if (mentor) {
            MentorProfile profile = mentorProfileRepository.findById(user.getId()).orElseGet(() -> {
                MentorProfile created = new MentorProfile();
                created.setUserId(user.getId());
                return created;
            });
            profile.setHeadline("Senior mobile/backend mentor");
            profile.setExpertise("React Native,Spring Boot,System Design");
            profile.setBasePrice(BigDecimal.valueOf(499000));
            profile.setVerificationStatus(MentorVerificationStatuses.VERIFIED);
            mentorProfileRepository.save(profile);
        }

        return user;
    }

    private void ensureBasicProfile(User user, String firstName, String lastName) {
        UserProfile profile = userProfileRepository.findById(user.getId()).orElseGet(() -> {
            UserProfile created = new UserProfile();
            created.setUserId(user.getId());
            return created;
        });
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setHeadline(firstName + " profile");
        profile.setLocation("Ho Chi Minh City");
        profile.setBio("Local development account");
        userProfileRepository.save(profile);
    }

    private void ensureMentorCatalog(User mentor) {
        if (servicePackageRepository.findByMentorId(mentor.getId()).isEmpty()) {
            ServicePackage servicePackage = new ServicePackage();
            servicePackage.setMentorId(mentor.getId());
            servicePackage.setName("Career Mentoring Starter");
            servicePackage.setDescription("Demo package for local mobile API testing");
            servicePackage.setIsActive(true);
            ServicePackage savedPackage = servicePackageRepository.save(servicePackage);

            ServicePackageVersion version = new ServicePackageVersion();
            version.setPackageId(savedPackage.getId());
            version.setPrice(BigDecimal.valueOf(799000));
            version.setDuration(60);
            version.setDeliveryType("ONLINE");
            version.setIsDefault(true);
            ServicePackageVersion savedVersion = servicePackageVersionRepository.save(version);

            PackageCurriculum intro = new PackageCurriculum();
            intro.setPackageVersionId(savedVersion.getId());
            intro.setTitle("Intro and goal alignment");
            intro.setDescription("Clarify goals and current blockers");
            intro.setOrderIndex(1);
            intro.setDuration(30);

            PackageCurriculum actionPlan = new PackageCurriculum();
            actionPlan.setPackageVersionId(savedVersion.getId());
            actionPlan.setTitle("Action plan");
            actionPlan.setDescription("Concrete next steps after the session");
            actionPlan.setOrderIndex(2);
            actionPlan.setDuration(30);

            packageCurriculumRepository.saveAll(List.of(intro, actionPlan));
        }
    }
}
