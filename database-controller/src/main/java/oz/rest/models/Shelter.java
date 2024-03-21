package oz.rest.models;

import java.util.ArrayList;
import java.util.Set;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

// import org.bson.codecs.pojo.annotations.BsonProperty;
import jakarta.validation.constraints.NotEmpty;
// import jakarta.validation.constraints.Pattern;

public class Shelter extends AbstractModel {
    // SRS says name as primary key, but should probably be email
    @NotEmpty(message = "Shelter name must not be empty")
    @BsonId()
    private String name;

    @NotEmpty(message = "Password must not be left empty")
    private String password;

    // TODO: Implement location
    // private String location;

    @BsonProperty("available_pet_ids")
    private Set<String> availablePetIds;

    // TODO: idk how to insert the regex that is in the SRS
    // @Pattern(regexp = "")
    // @BsonProperty("phone_number")
    // private String phoneNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getAvailablePetIds() {
        return availablePetIds;
    }

    public void setAvailablePetIds(Set<String> availablePetIds) {
        this.availablePetIds = availablePetIds;
    }

    public void addAvailablePetId(String newPetIds) {
        this.availablePetIds.add(newPetIds);
    }

    public void removeAvailablePetId(String toRemove) {
        this.availablePetIds.remove(toRemove);
    }

    // likely not needed, i think we always add pets one at a time rather than in
    // bulk

    // public void addAvailablePetIds(ArrayList<String> newPetIds) {
    // this.availablePetIds.addAll(newPetIds);
    // }

    // public void removeAvailablePetIds(ArrayList<String> toRemove) {
    // this.availablePetIds.removeAll(toRemove);
    // }
}
