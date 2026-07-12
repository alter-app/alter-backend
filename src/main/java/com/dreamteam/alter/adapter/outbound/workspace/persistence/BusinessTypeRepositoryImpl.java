package com.dreamteam.alter.adapter.outbound.workspace.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.dreamteam.alter.domain.workspace.entity.BusinessType;
import com.dreamteam.alter.domain.workspace.port.outbound.BusinessTypeRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BusinessTypeRepositoryImpl implements BusinessTypeRepository {

    private final BusinessTypeJpaRepository businessTypeJpaRepository;

    @Override
    public Optional<BusinessType> findById(Long id) {
        return businessTypeJpaRepository.findById(id);
    }

    @Override
    public BusinessType save(BusinessType businessType) {
        return businessTypeJpaRepository.save(businessType);
    }

    @Override
    public void delete(BusinessType businessType) {
        businessTypeJpaRepository.delete(businessType);
    }

    @Override
    public boolean existsByName(String name) {
        return businessTypeJpaRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return businessTypeJpaRepository.existsByNameAndIdNot(name, id);
    }
}
