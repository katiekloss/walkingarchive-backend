package org.walkingarchive.backend.controller;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.walkingarchive.backend.model.card.CardDAO;
import org.walkingarchive.backend.model.card.Deck;
import org.walkingarchive.backend.model.security.SecurityDAO;
import org.walkingarchive.backend.model.security.User;

@Path("/deck/")
public class DeckController {
    @Context
    protected ServletContext context;
    @Context
    protected HttpServletRequest request;
    @Context
    protected HttpServletResponse response;
    @Context
    protected UriInfo uriInfo;

    @GET
    @Path("user/{userId}")
    public List<Deck> getDecksByUser(@PathParam("userId") String userId) {
        //TODO - validate input
        User user = SecurityDAO.getInstance().getUserById(Integer.parseInt(userId));
        return CardDAO.getInstance().getDecks(user);
    }
}
