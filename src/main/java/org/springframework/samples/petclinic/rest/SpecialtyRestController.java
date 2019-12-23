/*
 * Copyright 2016-2017 the original author or authors.
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

package org.springframework.samples.petclinic.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * @author Vitaliy Fedoriv
 *
 */

@CrossOrigin(exposedHeaders = "errors, content-type")
@Path("api/specialties")
public class SpecialtyRestController {

	@Inject
	private ClinicService clinicService;

	@Inject
	private Validator validator;

    @PreAuthorize( "hasRole(@roles.VET_ADMIN)" )
	@GET
	@Path("")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllSpecialtys(){
		Collection<Specialty> specialties = new ArrayList<Specialty>();
		specialties.addAll(this.clinicService.findAllSpecialties());
		if (specialties.isEmpty()){
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(specialties).build();
	}

    @PreAuthorize( "hasRole(@roles.VET_ADMIN)" )
	@GET
	@Path("/{specialtyId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSpecialty(@PathParam("specialtyId") int specialtyId){
		Specialty specialty = this.clinicService.findSpecialtyById(specialtyId);
		if(specialty == null){
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(specialty).build();
	}

    @PreAuthorize( "hasRole(@roles.VET_ADMIN)" )
	@POST
	@Path("")
	@Produces( MediaType.APPLICATION_JSON)
	public Response addSpecialty( @Valid Specialty specialty) { //}, BindingResult bindingResult, UriComponentsBuilder ucBuilder){
		Set<ConstraintViolation<Specialty>> errors = validator.validate(specialty);
		if (!errors.isEmpty() || (specialty == null)) {
			return Response.status(Status.BAD_REQUEST).entity(specialty).header("errors", errors.stream().collect(Collectors.toMap(ConstraintViolation::getPropertyPath, ConstraintViolation::getMessage))).build();
		}
		this.clinicService.saveSpecialty(specialty);
		// headers.setLocation(ucBuilder.path("/api/specialtys/{id}").buildAndExpand(specialty.getId()).toUri()); //TODO
		return Response.status(Status.CREATED).entity(specialty).build();
	}

    @PreAuthorize( "hasRole(@roles.VET_ADMIN)" )
	@PUT
	@Path("/{specialtyId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateSpecialty(@PathParam("specialtyId") int specialtyId,@Valid Specialty specialty) { //}, BindingResult bindingResult){
		Set<ConstraintViolation<Specialty>> errors = validator.validate(specialty);
		if (!errors.isEmpty() || (specialty == null)) {
			return Response.status(Status.BAD_REQUEST).entity(specialty).header("errors", errors.stream().collect(Collectors.toMap(ConstraintViolation::getPropertyPath, ConstraintViolation::getMessage))).build();
		}
		Specialty currentSpecialty = this.clinicService.findSpecialtyById(specialtyId);
		if(currentSpecialty == null){
			return Response.status(Status.NOT_FOUND).build();
		}
		currentSpecialty.setName(specialty.getName());
		this.clinicService.saveSpecialty(currentSpecialty);
		return Response.noContent().entity(currentSpecialty).build();
	}

    @PreAuthorize( "hasRole(@roles.VET_ADMIN)" )
	@DELETE
	@Path("/{specialtyId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response deleteSpecialty(@PathParam("specialtyId") int specialtyId){
		Specialty specialty = this.clinicService.findSpecialtyById(specialtyId);
		if(specialty == null){
			return Response.status(Status.NOT_FOUND).build();
		}
		this.clinicService.deleteSpecialty(specialty);
		return Response.noContent().build();
	}

}
