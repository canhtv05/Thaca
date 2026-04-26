package com.thaca.auth.internal.services;

import com.thaca.auth.domains.Permission;
import com.thaca.auth.domains.Role;
import com.thaca.auth.domains.SystemUser;
import com.thaca.auth.domains.User;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.mappers.PermissionMapper;
import com.thaca.auth.mappers.RoleMapper;
import com.thaca.auth.mappers.UserMapper;
import com.thaca.auth.repositories.PermissionRepository;
import com.thaca.auth.repositories.RoleRepository;
import com.thaca.auth.repositories.SystemCredentialRepository;
import com.thaca.auth.repositories.UserRepository;
import com.thaca.common.constants.InternalMethod;
import com.thaca.common.dtos.internal.AuthUserDTO;
import com.thaca.common.dtos.internal.PermissionDTO;
import com.thaca.common.dtos.internal.RoleDTO;
import com.thaca.common.dtos.internal.UserDTO;
// import com.thaca.auth.services.KafkaProducerService;
import com.thaca.common.dtos.internal.VerifyEmailTokenDTO;
// import com.thaca.framework.blocking.starter.configs.cache.RedisCacheService;
// import com.thaca.framework.blocking.starter.services.SessionStore;
import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalService {

    private final UserRepository userRepository;
    private final SystemCredentialRepository systemCredentialRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    // private final KafkaProducerService kafkaProducerService;
    // private final RedisCacheService redisService;
    // private final SessionStore sessionStore;

    @FwMode(name = InternalMethod.INTERNAL_CMS_ACTIVE_USER, type = ModeType.VALIDATE)
    public void validateActiveUserByUserName(VerifyEmailTokenDTO request) {
        if (request.email().contains("+")) {
            throw new FwException(ErrorMessage.EMAIL_INVALID);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_ACTIVE_USER, type = ModeType.HANDLE)
    public VerifyEmailTokenDTO activeUserByUserName(VerifyEmailTokenDTO request) {
        User user = userRepository
            .findByUsername(request.username())
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        user.setIsActivated(true);
        userRepository.save(user);

        // kafkaProducerService.sendAndWait(
        // EventConstants.USER_CREATED_TOPIC,
        // user.getUsername(),
        // new UserCreationEvent(user.getUsername(), request.fullname())
        // );

        return new VerifyEmailTokenDTO(
            user.getUsername(),
            request.fullname(),
            request.email(),
            request.expiredAt(),
            request.jti()
        );
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_PROFILE, type = ModeType.HANDLE)
    public AuthUserDTO getSystemProfile() {
        String username = SecurityUtils.getCurrentUsername();
        return systemCredentialRepository
            .findByUsername(username)
            .map(sc -> {
                SystemUser su = sc.getSystemUser();
                return AuthUserDTO.builder()
                    .id(su.getId())
                    .username(sc.getUsername())
                    .email(su.getEmail())
                    .fullname(su.getFullname())
                    .isActivated(su.getIsActivated())
                    .isLocked(su.getIsLocked())
                    .isSuperAdmin(su.getIsSuperAdmin())
                    .avatarUrl(su.getAvatarUrl())
                    .roles(sc.getRoles().stream().map(Role::getCode).collect(Collectors.toSet()))
                    .build();
            })
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_USERS, type = ModeType.HANDLE)
    public SearchResponse<UserDTO> searchUsers(SearchRequest<UserDTO> request) {
        Specification<User> spec = createUserSpecification(request);
        Page<User> users = userRepository.findAll(spec, request.getPage().toPageable(Sort.Direction.DESC, "createdAt"));
        return new SearchResponse<>(
            users
                .getContent()
                .stream()
                .map(u -> UserMapper.fromEntityWithCms(u, true))
                .collect(Collectors.toList()),
            PaginationResponse.of(users)
        );
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_ROLES, type = ModeType.HANDLE)
    public SearchResponse<RoleDTO> searchRoles(SearchRequest<RoleDTO> request) {
        Specification<Role> spec = createRoleSpecification(request);
        Page<Role> roles = roleRepository.findAll(spec, request.getPage().toPageable());
        return new SearchResponse<>(
            roles.getContent().stream().map(RoleMapper::fromEntity).collect(Collectors.toList()),
            PaginationResponse.of(roles)
        );
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_PERMISSIONS, type = ModeType.HANDLE)
    public SearchResponse<PermissionDTO> searchPermissions(SearchRequest<PermissionDTO> request) {
        String roleCode = request.getFilter() != null ? request.getFilter().getRoleCode() : null;
        String roleDescription;
        if (StringUtils.isNotBlank(roleCode)) {
            roleDescription = roleRepository.findByCode(roleCode).map(Role::getDescription).orElse(null);
        } else {
            roleDescription = null;
        }
        Specification<Permission> spec = createPermissonSpecification(request);
        Page<Permission> permissions = permissionRepository.findAll(spec, request.getPage().toPageable());
        List<PermissionDTO> content = permissions
            .getContent()
            .stream()
            .map(p -> PermissionMapper.fromEntity(p, roleDescription))
            .toList();
        return new SearchResponse<>(content, PaginationResponse.of(permissions));
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_USER_BY_ID, type = ModeType.HANDLE)
    public UserDTO findById(Long id) {
        return userRepository
            .findById(id)
            .map(u -> UserMapper.fromEntityWithCms(u, true))
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_LOCK_USER, type = ModeType.HANDLE)
    public void lockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        user.setIsLocked(true);
        userRepository.save(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_UNLOCK_USER, type = ModeType.HANDLE)
    public void unlockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        user.setIsLocked(false);
        userRepository.save(user);
    }

    private Specification<User> createUserSpecification(SearchRequest<UserDTO> req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (req.getFilter() != null) {
                UserDTO filter = req.getFilter();
                if (StringUtils.isNotBlank(filter.getUsername())) {
                    predicates.add(
                        cb.like(cb.lower(root.get("username")), "%" + filter.getUsername().toLowerCase() + "%")
                    );
                }
                if (StringUtils.isNotBlank(filter.getEmail())) {
                    predicates.add(cb.like(cb.lower(root.get("email")), "%" + filter.getEmail().toLowerCase() + "%"));
                }
                if (Objects.nonNull(filter.getIsActivated())) {
                    predicates.add(cb.equal(root.get("isActivated"), filter.getIsActivated()));
                }
                if (Objects.nonNull(filter.getIsLocked())) {
                    predicates.add(cb.equal(root.get("isLocked"), filter.getIsLocked()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Role> createRoleSpecification(SearchRequest<RoleDTO> req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (req.getFilter() != null) {
                RoleDTO filter = req.getFilter();
                if (StringUtils.isNotBlank(filter.getCode())) {
                    predicates.add(cb.like(cb.lower(root.get("code")), "%" + filter.getCode().toLowerCase() + "%"));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Permission> createPermissonSpecification(SearchRequest<PermissionDTO> req) {
        return (root, query, cb) -> {
            Join<Permission, Role> roleJoin;
            if (Permission.class.equals(query.getResultType())) {
                root.fetch("roles", JoinType.LEFT);
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (req.getFilter() != null) {
                PermissionDTO filter = req.getFilter();
                if (StringUtils.isNotBlank(filter.getCode())) {
                    predicates.add(cb.like(cb.lower(root.get("code")), "%" + filter.getCode().toLowerCase() + "%"));
                }
                if (StringUtils.isNotBlank(filter.getRoleCode())) {
                    roleJoin = root.join("roles", JoinType.INNER);
                    predicates.add(cb.equal(roleJoin.get("code"), filter.getRoleCode()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
