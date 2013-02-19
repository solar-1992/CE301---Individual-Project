package Project.Game.Behaviours.Constructive;

import Project.Game.Blueprints.Blueprint;
import Project.Game.Blueprints.BlueprintBuilding;
import Project.Game.Entities.Entity;
import Project.Game.Faction;
import Project.Game.Main;
import Project.Game.Resource.ResourceDrain;
import Project.Game.Resource.ResourcePool;
import Project.Game.Vector2D;

import java.util.HashMap;

/**
 *
 */
public class BlueprintConstruction extends BasicConstruction {
    private Faction faction;
    private Vector2D location;

    private Blueprint blueprint;

    // Use the offset as a key to locate buildings
    private HashMap<Vector2D, Entity> buildings;

    private BlueprintState state;

    private int ticksTillFinished;

    public BlueprintConstruction(Faction faction, Entity entity, ResourcePool resourcePool, Vector2D location, Blueprint blueprint) {
        super(faction, entity, resourcePool);
        this.faction = faction;
        this.location = location;
        this.blueprint = blueprint;

        buildings = new HashMap<>();

        state = BlueprintState.Looking;
    }

    @Override
    public Vector2D getSpawnPoint() {
        return Main.VECTOR2D_SOURCE.getVector(0, 0);
    }

    @Override
    public void update() {
        switch (state) {
            case Looking:
                // Find the next building to build
                findAndBuildNext();
                break;
            case AllConstructed:
                break;
            case Constructing:
                break;
        }

    }

    private void findAndBuildNext() {
        Vector2D centerLocation = getEntity().getMovementBehaviour().getLocation();

        for (BlueprintBuilding blueprintBuilding : blueprint.getBlueprintBuildings()) {
            // Only find first one
            if (!buildings.containsKey(blueprintBuilding.getOffset())) {
                resourceDrain.deRegister();
                // Need to put this type of information into the blueprint
                resourceDrain = new ResourceDrain(resourcePool, 10);
                break;
            }
        }
    }
    private void constructionWork(){
        ticksTillFinished++;

        if(ticksTillFinished == 0){
            // Switch state
        }
    }
}

enum BlueprintState {
    Constructing,
    AllConstructed,
    Looking
}
