package com.github.gervaisb.demo.product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import com.github.gervaisb.demo.tenant.TenantId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@ToString
public class Product {

    @Id
    private String key;

    private ProductState state;

    public Product(TenantId tenantId, ProductId productId, ProductOwnerId productOwnerId,
                   String name, String description) {
        ProductState state = new ProductState();
        state.productKey = encodeProductKey(tenantId, productId);
        state.productOwnerId = productOwnerId;
        state.name = name;
        state.description = description;
        state.backlogItems = new ArrayList<>();
        setState(state);
        this.key = state.productKey;
    }

    private Product() {
        /* Jpa */
    }

    private void setState(ProductState state) { /* Jpa */
        assert state!=null : "Product state cannot be null";
        this.state = state;
        this.key = state.productKey;
    }

    public ProductId getProductId() {
        return decodeProductId(state);
    }

    public TenantId getTenantId() {
        return decodeTenantId(state);
    }

    public String getName() {
        return state.name;
    }

    public String getDescription() {
        return state.description;
    }

    public List<BacklogItem> getBacklogItems() {
        List<BacklogItem> all = state.backlogItems.stream()
                .map(BacklogItemState::toProductBacklogItem)
                .collect(Collectors.toList());
        return Collections.unmodifiableList(all);
    }

    public BacklogItem getBacklogItem(BacklogItemId backlogItemId) {
        return state.backlogItems.stream()
                .filter(i -> i.key.equals(backlogItemId.value))
                .findFirst()
                .map(BacklogItemState::toProductBacklogItem)
                .get();
    }

    void addBacklogItem(BacklogItem productBacklogItem) {
        productBacklogItem.state.key = state.productKey+"-"+(state.backlogItems.size()+1);
        this.state.backlogItems.add(productBacklogItem.state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return key.equals(product.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    boolean hasSameState(Product other) {
        return state.equals(other.state);
    }

    static String encodeProductKey(TenantId tenantId, ProductId productId) {
        return tenantId.value + ":" +productId.value;
    }

    private static ProductId decodeProductId(ProductState state) {
        return new ProductId(state.productKey.split(":")[1]);
    }

    private static TenantId decodeTenantId(ProductState state) {
        return new TenantId(state.productKey.split(":")[0]);
    }
}

@Data
@Embeddable
@EqualsAndHashCode(exclude = {"backlogItems"})
class ProductState {
    String productKey;
    ProductOwnerId productOwnerId;
    String name;
    String description;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name = "backlog")
    List<BacklogItemState> backlogItems;

}
