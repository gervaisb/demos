package com.github.gervaisb.demo.product;

import com.github.gervaisb.demo.tenant.TenantId;
import org.assertj.core.api.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductServiceIT {

	@Autowired
	private ProductsService subject;

	private final ProductOwnerId productOwnerId = new ProductOwnerId();
	private final TenantId tenantId = new TenantId();

	@Test
	public void should_create_and_retrieve_product() {
		Product created = subject.create(tenantId, productOwnerId, "IT", "IT Product");
		Product retrieved = subject.get(tenantId, created.getProductId());

		assertThat(created)
				.isNotSameAs(retrieved)
				.isEqualTo(retrieved)
				.has(sameStateAs(retrieved));
	}

	private static Condition<Product> sameStateAs(Product other) {
		return new Condition<>(
				product -> product.hasSameState(other),
				"Same state as \n <"+other+">"
		);
	}
}
