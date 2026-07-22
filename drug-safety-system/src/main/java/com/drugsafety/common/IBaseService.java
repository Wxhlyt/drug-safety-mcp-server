package com.drugsafety.common;

import java.util.List;

public interface IBaseService<T> {

    T getById(Long id);

    List<T> listAll();

    T create(T entity);

    T update(T entity);

    void deleteById(Long id);
}