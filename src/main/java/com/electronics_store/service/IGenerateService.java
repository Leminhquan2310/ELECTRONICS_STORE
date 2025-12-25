package com.electronics_store.service;

import org.springframework.data.crossstore.ChangeSetPersister;

import java.util.List;
import java.util.Optional;

public interface IGenerateService<T> {
    T create(Object t) throws ChangeSetPersister.NotFoundException;

    List<T> createBatch(List<T> ts);

    T getById(Long id);

    List<T> getAll();

    T update(Object t);

    boolean delete(Long id);
}
