package oz.rest.services;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonArray;
import jakarta.websocket.server.PathParam;
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

// TODO: i think we need to make email address unique between adopter and shelters,
// so user can use a generic "login" page and then just navigate to whichever
// functionality they need, likely involves a generic "user" class and having adopteruser
// and shelteruser inherit from superclass; remember to deal with permissions in JWT etc
@Tag(name = "Shelters")
@Path("/shelter")
@ApplicationScoped
public class ShelterService extends AbstractService<Shelter> {
    // TODO: getting the collection is done for every service,
    // so it would be nice to have that moved into the AbstractService
    // as a field somehow, and initialized in each service
    @Override
    @POST
    @APIResponses({
            @APIResponse(responseCode = "400", description = "The request was invalid"),
            @APIResponse(responseCode = "200", description = "Successfully added new shelter")
    })
    @Operation(summary = "Add a new shelter to the database")
    public Response add(Shelter newEntry) {
        JsonArray violations = getViolations(newEntry);

        if (!violations.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(violations.toString())
                    .build();
        }

        MongoCollection<Shelter> sheltersCollection = db.getCollection("Shelters",
                Shelter.class);
        sheltersCollection.insertOne(newEntry);

        return Response
                .status(Response.Status.OK)
                .entity(newEntry.toJson())
                .build();
    }

    @Override
    @GET
    public Response retrieve(@QueryParam(value = "id") String id) {
        MongoCollection<Shelter> sheltersCollection = db.getCollection("Shelters",
                Shelter.class);

        var shelter = sheltersCollection.find(eq("_id", id)).first();
        if (shelter == null) {
            return Response.status(400).build();
        } else {
            return Response.ok(shelter.toJson()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({ @APIResponse(responseCode = "200", description = "Successfully updatedshelter."),
            @APIResponse(responseCode = "400", description = "Invalid name orconfiguration"),
            @APIResponse(responseCode = "404", description = "Shelter not found")

    })

    @Operation(summary = "Update info about a shelter")
    public Response update(@PathParam("idType") Integer idType, String part) {
        JsonArray vio = getViolations(shelter);

        if (!vio.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(vio.toString())
                    .build();
        }
        MongoCollection<Shelter> shelters = db.getCollection("Shelters",
                Shelter.class);
        // shelter = shelters.find(and(eq("_id", shelter.getName()), eq("password",
        // shelter.getPassword()))).first();

        Shelter newShelter = new Shelter();
        newShelter.setName(shelter.getName());
        newShelter.setPassword(shelter.getPassword());
        newShelter.setAvailablePets(shelter.getAvailablePets());

        UpdateResult updateResult = shelters
                .replaceOne(and(eq("_id", shelter.getName()), eq("password",
                        shelter.getPassword())), newShelter);

        if (updateResult.getMatchedCount() == 0) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("[\"_id was not found!\"]")
                    .build();
        }

        shelters.replaceOne(and(eq("_id", shelter.getName()), eq("password",
                shelter.getPassword())), newShelter);

        return Response
                .status(Response.Status.OK)
                .entity(newShelter.toJson())
                .build();
    }

    @Override
    @DELETE
    public Response remove(@QueryParam(value = "id") String id) {
        MongoCollection<Shelter> sheltersCollection = db.getCollection("Shelters",
                Shelter.class);

        var removedShelter = sheltersCollection.findOneAndDelete(eq("_id", id));
        if (removedShelter == null) {
            return Response.status(400).build();
        } else {
            return Response.ok(removedShelter.toJson()).build();
        }
    }

    @Path("/login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Login as a shelter user")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Login was successful"),
            @APIResponse(responseCode = "401", description = "Login failed")
    })
    public Response login(@QueryParam(value = "name") String name, @QueryParam(value = "password") String password) {
        MongoCollection<Shelter> sheltersCollection = db.getCollection("Shelters",
                Shelter.class);

        // TODO: encrypt passwords at rest, java.security MessageDigest looks promising

        var record = sheltersCollection.find(and(eq("_id", name), eq("password", password))).first();

        if (record == null) {
            return Response.status(401).build();
        }

        return Response.ok(record.toJson()).build();
    }
}
