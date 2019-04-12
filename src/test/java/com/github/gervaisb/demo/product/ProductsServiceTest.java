package com.github.gervaisb.demo.product;

import java.util.HashSet;

import com.github.gervaisb.demo.tenant.TenantId;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ProductsServiceTest {

    private final ProductRepository repository = mock(ProductRepository.class);
    private final ProductsService subject = new ProductsService(repository);

    private final ProductOwnerId productOwnerId = new ProductOwnerId();
    private final TenantId tenantId = new TenantId();

    @Test
    public void create_must_persist_and_return_created_product() {
        Product product = subject.create(tenantId, productOwnerId, "Test", "Test product");

        assertThat(product)
                .isNotNull()
                .hasFieldOrPropertyWithValue("Name", "Test")
                .hasFieldOrPropertyWithValue("Description", "Test product")
                .hasFieldOrPropertyWithValue("TenantId", tenantId)
                .hasFieldOrProperty("ProductId");
        verify(repository).save(product);
    }

    @Test
    public void get_must_retrieve_product_in_tenant() {
        ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
        when(repository.findByKey(keys.capture()))
                .thenReturn(new DummyProduct());

        ProductId productId = new ProductId();

        subject.get(tenantId, productId);
        subject.get(tenantId, productId);
        subject.get(new TenantId(), productId);

        assertThat(new HashSet<>(keys.getAllValues()))
                .hasSize(2); // Different tenants produce different keys
    }

    private class DummyProduct extends Product {
        DummyProduct() {
            super(tenantId, new ProductId(), productOwnerId, "Dummy", "Dummy product");
        }
    }
}
