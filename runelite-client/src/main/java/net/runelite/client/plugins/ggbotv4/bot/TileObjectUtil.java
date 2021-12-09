package net.runelite.client.plugins.ggbotv4.bot;

import net.runelite.api.*;
import net.runelite.client.RuneLite;

public class TileObjectUtil {
    public static TileObject findTileObject(Tile tile, int id)
    {
        if (tile == null)
        {
            return null;
        }

        final GameObject[] tileGameObjects = tile.getGameObjects();
        final DecorativeObject tileDecorativeObject = tile.getDecorativeObject();
        final WallObject tileWallObject = tile.getWallObject();
        final GroundObject groundObject = tile.getGroundObject();

        if (objectIdEquals(tileWallObject, id))
        {
            return tileWallObject;
        }

        if (objectIdEquals(tileDecorativeObject, id))
        {
            return tileDecorativeObject;
        }

        if (objectIdEquals(groundObject, id))
        {
            return groundObject;
        }

        for (GameObject object : tileGameObjects)
        {
            if (objectIdEquals(object, id))
            {
                return object;
            }
        }

        return null;
    }

    public static boolean objectIdEquals(TileObject tileObject, int id)
    {
        final Client client = RuneLite.getInjector().getInstance(Client.class);

        if (tileObject == null)
        {
            return false;
        }

        if (tileObject.getId() == id)
        {
            return true;
        }

        // Menu action EXAMINE_OBJECT sends the transformed object id, not the base id, unlike
        // all the GAME_OBJECT_OPTION actions, so check the id against the impostor ids
        final ObjectComposition comp = client.getObjectDefinition(tileObject.getId());

        if (comp.getImpostorIds() != null)
        {
            for (int impostorId : comp.getImpostorIds())
            {
                if (impostorId == id)
                {
                    return true;
                }
            }
        }

        return false;
    }
}
