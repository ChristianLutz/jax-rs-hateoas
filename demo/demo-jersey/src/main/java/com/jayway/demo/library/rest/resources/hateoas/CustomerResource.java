/*
 * Copyright 2011 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jayway.demo.library.rest.resources.hateoas;

import com.jayway.demo.library.domain.BookRepository;
import com.jayway.demo.library.domain.Customer;
import com.jayway.demo.library.domain.CustomerRepository;
import com.jayway.demo.library.domain.factory.RepositoryFactory;
import com.jayway.demo.library.rest.dto.CustomerDto;
import com.jayway.demo.library.rest.dto.LoanDto;
import com.jayway.jaxrs.hateoas.Linkable;
import com.jayway.jaxrs.hateoas.ParamExpander;
import com.jayway.jaxrs.hateoas.core.HateoasResponse;
import com.jayway.jaxrs.hateoas.support.AtomRels;
import com.jayway.jaxrs.hateoas.support.FieldPath;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Path("/library/customers")
public class CustomerResource {

    private CustomerRepository customerRepository;
    private BookRepository bookRepository;

    public CustomerResource() {
        customerRepository = RepositoryFactory.getCustomerRepository();
        bookRepository = RepositoryFactory.getBookRepository();
    }

    @GET
    @Produces("application/vnd.demo.library.list.customer+json")
    @Linkable(LinkableIds.CUSTOMER_LIST_ID)
    public Response getAllCustomers() {
        return HateoasResponse
                .ok(CustomerDto.fromBeanCollection(customerRepository.getAllCustomers()))
                .selfLink(LinkableIds.CUSTOMER_NEW_ID)
                .each(LinkableIds.CUSTOMER_DETAILS_ID, AtomRels.VIA,  ParamExpander.field("id"), ParamExpander.queryParam("type", "FOOZZ"))
                .selfEach(LinkableIds.CUSTOMER_DETAILS_ID, ParamExpander.value("custom"), ParamExpander.queryParam("foo-1", "bar"))
                .link(LinkableIds.CUSTOMER_DETAILS_ID, "REL", ParamExpander.value("DUMMY-ID"), ParamExpander.queryParam("foo-2", "bar"))
                .link(FieldPath.path("rows"), LinkableIds.CUSTOMER_DETAILS_ID, "FP", ParamExpander.value("PATH-ID"))

                .build();

    }

    @POST
    @Consumes("application/vnd.demo.library.customer+json")
    @Produces("application/vnd.demo.library.customer+json")
    @Linkable(value = LinkableIds.CUSTOMER_NEW_ID, templateClass = CustomerDto.class)
    public Response newCustomer(CustomerDto customer) {
        Customer newCustomer = customerRepository.newCustomer(customer.getName());
        return HateoasResponse
                .created(LinkableIds.CUSTOMER_DETAILS_ID, newCustomer.getId())
                .selfLink(LinkableIds.CUSTOMER_DETAILS_ID, newCustomer.getId())
                .entity(CustomerDto.fromBean(newCustomer)).build();
    }

    @GET
    @Path("/{id}")
    @Produces("application/vnd.demo.library.customer+json")
    @Linkable(LinkableIds.CUSTOMER_DETAILS_ID)
    public Response getCustomer(@PathParam("id") Integer id,
                                @QueryParam("type") @DefaultValue("DEF") String type) {
        return HateoasResponse
                .ok(CustomerDto.fromBean(customerRepository.getById(id)))
                .link(LinkableIds.CUSTOMER_LOANS_ID, Rels.LOANS, id).build();
    }

    @GET
    @Path("/{id}/loans")
    @Produces("application/vnd.demo.library.list.loan+json")
    @Linkable(LinkableIds.CUSTOMER_LOANS_ID)
    public Response getCustomerLoans(@PathParam("id") Integer id) {
        Collection<LoanDto> loanDtos = LoanDto
                .fromBeanCollection(bookRepository.getLoansForCustomer(id));
        return HateoasResponse.ok(loanDtos)
                              .selfEach(LinkableIds.LOAN_DETAILS_ID, "bookId").build();
    }
}
