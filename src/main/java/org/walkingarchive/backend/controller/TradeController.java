package org.walkingarchive.backend.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.walkingarchive.backend.model.card.Card;
import org.walkingarchive.backend.model.card.CardFactory;
import org.walkingarchive.backend.model.security.SecurityFactory;
import org.walkingarchive.backend.model.security.User;
import org.walkingarchive.backend.model.trade.Trade;
import org.walkingarchive.backend.model.trade.TradeFactory;

@Path("/trade/")
public class TradeController {
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
    @Produces(MediaType.APPLICATION_JSON)
    public List<Trade> getTradeByUser(@PathParam("userId") String userId) {
        //TODO - validate input
        return TradeFactory.getInstance().getTradesForUser(userId);
    }

    @GET
    @Path("date/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Trade> getTradeByUser(@PathParam("date") Long date) {
        //TODO - validate input
        Date tradeDate = new Date(date);
        return TradeFactory.getInstance().getTradesForDate(tradeDate);
    }
    
    @PUT
    @Path("add")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTrade(String json) throws Exception {
        JSONObject jsonObject = new JSONObject(json);
        User user = SecurityFactory.getInstance().getUserById(jsonObject.getInt("user"));
        
        Trade trade = new Trade(user, new ArrayList<Card>(), new ArrayList<Card>(), true);
        trade = TradeFactory.getInstance().createTrade(trade);
        
        return Response.ok(trade, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Path("update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(String json) throws Exception {
        JSONObject jsonObject = new JSONObject(json);
        Trade trade = TradeFactory.getInstance().getTradeForId(jsonObject.getInt("id"));
        trade.setActive(true);
        JSONArray cardsGiving = jsonObject.getJSONArray("givingCards");
        JSONArray cardsReceiving = jsonObject.getJSONArray("receivingCards");
        
        for(int i = 0; i < cardsGiving.length(); i++) {
            Card card = CardFactory.getInstance().getCard(cardsGiving.getInt(i));
            trade.addCardToGiving(card);
        }
        for(int i = 0; i < cardsReceiving.length(); i++) {
            Card card = CardFactory.getInstance().getCard(cardsReceiving.getInt(i));
            trade.addCardToReceiving(card);
        }
        
        trade = TradeFactory.getInstance().updateTrade(trade);
        
        return Response.ok(trade, MediaType.APPLICATION_JSON).build();
    }
}
