package com.github.gervaisb.demo.product;

import org.springframework.data.repository.CrudRepository;

interface ProductRepository extends CrudRepository<Product, Long> {

    Product findByKey(String key);
}
