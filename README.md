# Modeling Aggregates with DDD and spring-data

We have all heard and suffered from the _object-relational impedance mismatch_ :
> The object-relational impedance mismatch is a set of conceptual and technical 
> difficulties that are often encountered when a relational database management 
> system (RDBMS) is being served by an application program (or multiple 
> application programs) written in an object-oriented programming language or 
> style, particularly because objects or class definitions must be mapped to 
> database tables defined by a relational schema.
> 
> -- https://en.wikipedia.org/wiki/Object-relational_impedance_mismatch

This mismatch is highlighted when you want to use JPA. It is worst when you 
want to apply the _domain driven design_ methodology because your clean 
aggregates suddenly become polluted with empty constructors, getters, setters, 
and adders.

In [Modeling Aggregates with DDD and Entity Framework](https://kalele.io/blog-posts/modeling-aggregates-with-ddd-and-entity-framework/),
Vaughn Vernon propose a separation between the state (that is persisted) and 
the behavior to reduce the object-relational impedance mismatch:

> The second approach uses a domain object backed by state objects. As shown in 
> Figure 6, the domain object defines and implements the domain-driven model 
> using the Ubiquitous Language, and the state objects hold the state of the 
> Aggregate.
> ```
>    +--------------+     +--------------+
>    |    Client    |----→|   Product    |
>    +--------------+     +--------------+
>                                 |
>                                 ↓
>                         +--------------+
>                         | ProductState |
>                         +--------------+
>                         
>    Figure 6. The domain object that models the Aggregate behavior is backed 
>    by a state object that holds the model’s state.
> ```
> 
> By keeping state objects separate from the domain-driven implementation 
> objects, it enables very simple mappings. We let Entity Framework to do what 
> it knows how to do by default to map entities to and from the database. [..]
> 
> -- https://kalele.io/blog-posts/modeling-aggregates-with-ddd-and-entity-framework

Do you remember the _[Memento pattern](https://en.wikipedia.org/wiki/Memento_pattern)_?  
The base idea of _"providing the ability to reset an object to its previous 
state_" is the same. While not for the same purpose, the same principle of 
splitting state and behavior is also applied in many actor based systems.

This is an interesting approach that I would like to test in a spring-boot 
application that honors the DDD principles and uses the Spring data module.

> Spring Data’s mission is to provide a familiar and consistent, Spring-based 
> programming model for data access while still retaining the special traits of 
> the underlying data store.
> 
> -- https://spring.io/projects/spring-data

I am going to reuse the same model as the one provided by Vaughn. A `Product`, 
identified by a `TenantId` and a `ProductId`, is managed by a `ProductOwnerId` 
and contains zero or many `BacklogItem`s.

    public class Product {
        private ProductState state;

        public Product(TenantId tenantId, ProductId productId, 
            ProductOwnerId productOwnerId, String name, String description) {
            // ...
        }

        public List<BacklogItem> getBacklogItems() {
            // ...
        }

        public ProductBacklogItem getBacklogItem(BacklogItemId backlogItemId) {
            // ...
        }

    }


_______________________________________________________________________________

## DDD vs spring data repositories

In _DDD_, a repository manage one aggregate (or entity). However, the spring 
data repository expects a JPA `@Entity`. Since our `Product` aggregate is not
annotated with `@Entity` and we want to keep it clean we have to find a solution.

### Solution 1, expose the state.
Whenever you want to save or retrieve an aggregate you deal with that 
difference:

    class ProductRepository implements CrudRepository<ProductState, ProductId> {
        // nothing
    }

    class ProductService {
        private final ProductRepository repository;

        public Product createAndGet(.*...*/) {
            Product product = new Product(/*..*/)
            repository.save(product.state);
            return repository.findOneById(product.productId)
                .map(Product::new)
                .get();
        }
    }
    
The access to the aggregate state can be restricted via the package-friendly 
visibility if you use packages by feature. But since we want to have a clean 
code base that embraces the DDD principles I would not recommend this approach. 

### Solution 2, composition of repository.
Compose your repositories with a spring data repository used as a supportive 
component. And expose the state.

    class ProductRepositorySupport implements CrudRepository<ProductState, ProductId> {
        // nothing
    }

    class ProductRepository {
        private final ProductRepositorySupport support;
        
        ProductRepository(ProductRepositorySupport support) {
            this.support = support;
        }

        void save(Product product) {
            support.save(product.state);
        }

        Product get(ProductId productId) {
            return support.findOneById(productId)
                .map(Product::new)
                .get(); 
        }
    }

    class ProductService {
        private final ProductRepository repository;
        
        public Product createAndGet(.*...*/) {
            Product product = new Product(/*..*/)
            repository.save(product);
            return repository.get(product.productId);
        }
    }
        
    
If we package by feature, with `Product` and `ProductRepository` in the same 
package, this may be the cleanest solution. The dependency on spring data is 
an implementation detail and we can offer the repository signatures that we 
want.

However, the same term "repository" is used form two different purpose in the 
same context. And the role of the supportive repository may not be clear.

### Solution 3, embeddable
JPA support the composition with the `@Embeddable` annotation. When one 
`@Entity` has an `@Embeddable` property, the last one is "merged" into the 
entity table.

If we use this pattern capability we can avoid the supportive repository and 
benefits of all the advantages of JPA without changing too much on our 
aggregate. 

    class ProductRepository implements CrudRepository<Product, ProductId> {
        // nothing
    }

    class ProductService {
        private final ProductRepository repository;
        
        public Product createAndGet(.*...*/) {
            Product product = new Product(/*..*/)
            repository.save(product);
            return repository.findOneById(product.productId).get();
        }
    }

    @Entity
    class Product {
        @Id
        private String key;
        @Embedded
        private ProductState state;
        private Product() { 
            // Jpa empty constructor
        }
        public Product(TenantId tenantId /* , ...*/) {
            ProductState state = new ProductState();
            state.tenantId = tenantId;
            // ..
            setState(state);
        }
        private setState(ProductState state) { // Used by Jpa 
            this.state = state;
            this.key = state.productKey;
        }
        // ..
    }

    @Emebddable 
    class ProductState {
        // ...
    }

You can see that our aggregate is changed for JPA. But those are small changes 
that make the model more explict, a newcomer will understand the pattern by 
looking at the model.

To make the exclusive link between the entity and his state class you can put 
both in the same source file, either as colocated or internal class.


If you are interested by the second approach, there is an excellent article 
by Vytautas Žurauskas on medium : [Two layer repositories in Spring](https://www.vzurauskas.com/2019/04/07/two-layer-repositories-in-spring/)
