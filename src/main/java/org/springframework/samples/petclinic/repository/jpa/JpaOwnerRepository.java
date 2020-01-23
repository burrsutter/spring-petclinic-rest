/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.repository.jpa;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.Audited;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

/**
 * JPA implementation of the {@link JpaOwnerRepository} interface.
 *
 * @author Mike Keith
 * @author Rod Johnson
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */

@ApplicationScoped
public class JpaOwnerRepository implements PanacheRepository<Owner> {

    @Inject
    EntityManager em;

    /**
     * Important: in the current version of this method, we load Owners with all
     * their Pets and Visits while we do not need Visits at all and we only need one
     * property from the Pet objects (the 'name' property). There are some ways to
     * improve it such as: - creating a Ligtweight class (example here:
     * https://community.jboss.org/wiki/LightweightClass) - Turning on lazy-loading
     * and using {@link OpenSessionInViewFilter}
     */
    @SuppressWarnings("unchecked")
    @Audited
    public Collection<Owner> findByLastName(String lastName) {
        // using 'join fetch' because a single query should load both owners and pets
        // using 'left join fetch' because it might happen that an owner does not have
        // pets yet
        Query query = this.em.createQuery(
                "SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName");
        query.setParameter("lastName", lastName + "%");
        return query.getResultList();
    }

    public Owner findById(long id) {
        // using 'join fetch' because a single query should load both owners and pets
        // using 'left join fetch' because it might happen that an owner does not have
        // pets yet
        Query query = this.em
                .createQuery("SELECT owner FROM Owner owner left join fetch owner.pets WHERE owner.id =:id");
        query.setParameter("id", id);
        return (Owner) query.getSingleResult();
    }

    @Audited
    public void save(Owner owner) {
        if (owner.getId() == null) {
            this.em.persist(owner);
        } else {
            this.em.merge(owner);
        }

    }

    @Override
    @Audited
    public void delete(Owner owner) {
        this.em.remove(this.em.contains(owner) ? owner : this.em.merge(owner));
    }

}
