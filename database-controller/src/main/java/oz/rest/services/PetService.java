package oz.rest.services;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonArray;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import oz.rest.models.Shelter;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.and;

import oz.rest.models.Pet;

import com.mongodb.client.MongoCollection;

@Tag(name = "Pets")
@Path("/pet")
@ApplicationScoped
public class PetService extends AbstractService<Pet> {
    @Override
    @POST
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Successfully added new pet"),
            @APIResponse(responseCode = "400", description = "The request was invalid"),
            @APIResponse(responseCode = "404", description = "Pet not found")
    })
    @Operation(summary = "Add a new pet to the database")
    public Response add(Pet newEntry) {
        JsonArray violations = getViolations(newEntry);

        if (!violations.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(violations.toString())
                    .build();
        }

        MongoCollection<Pet> petCollection = db.getCollection("Pets",
                Pet.class);

        MongoCollection<Shelter> shelterCollection = db.getCollection("Shelters",
                Shelter.class);

        var shelter = shelterCollection.find(eq("_id", newEntry.getCurrentShelterId())).first();
        if (shelter == null) {
            return Response.status(404).build();
        }

        // add pet to shelter
        shelter.addAvailablePetId(newEntry.getCurrentShelterId());
        petCollection.insertOne(newEntry);

        return Response
                .status(Response.Status.OK)
                .entity(newEntry.toJson())
                .build();
    }

    @Override
    @GET
    public Response retrieve(@QueryParam(value = "id") String id) {
        // var a = validator.validate(idString);

        MongoCollection<Pet> pets = db.getCollection("Pets", Pet.class);
        var pet = pets.find(eq("_id", id)).first();

        if (pet == null) {
            return Response.status(404).build();
        }

        return Response.ok(pet.toJson()).build();
    }

    @Override
    @DELETE
    public Response remove(@QueryParam(value = "id") String id) {
        MongoCollection<Shelter> petCollection = db.getCollection("Pets",
                Shelter.class);

        var removedPet = petCollection.findOneAndDelete(eq("_id", id));
        if (removedPet == null) {
            return Response.status(404).build();
        }

        return Response.ok(removedPet).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Successfully updated shelter."),
            @APIResponse(responseCode = "400", description = "Invalid name or configuration"),
            @APIResponse(responseCode = "404", description = "Shelter not found")
    })
    @Operation(summary = "Update a pet in a shelter")
    public Response updatePet(@QueryParam(value = "pet_id") String id,
            @Parameter(description = "New pet data") Pet newPet) {
        JsonArray violations = getViolations(newPet);

        if (!violations.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(violations.toString())
                    .build();
        }

        return null;
    }
}
