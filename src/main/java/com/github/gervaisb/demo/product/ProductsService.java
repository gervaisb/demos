package com.github.gervaisb.demo.product;

import com.github.gervaisb.demo.tenant.TenantId;

import org.springframework.stereotype.Service;

@Service
public class ProductsService {

    private final ProductRepository repository;

    public ProductsService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product create(TenantId tenantId, ProductOwnerId productOwnerId, String name, String description) {
        Product product = new Product(tenantId, new ProductId(), productOwnerId, name, description);
        product.addBacklogItem(new BacklogItem("First item"));
        repository.save(product);
        return product;
    }

    public Product get(TenantId tenantId, ProductId productId) {
        return repository.findByKey(Product.encodeProductKey(tenantId, productId));
    }

}
