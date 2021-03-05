package org.golde.discordbot.website.server.routes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.golde.discordbot.shared.ESSBot;
import org.golde.discordbot.shared.constants.Roles;
import org.golde.discordbot.website.WebsiteBot;
import org.golde.discordbot.website.server.routes.base.AbstractJsonResponse;

import com.google.gson.JsonObject;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class PageAllMembers extends AbstractJsonResponse {

	@Override
	public JsonObject getResponse(Map<String, String> urlParams, IHTTPSession session, JsonObject root) {

		if(session.getMethod() != Method.GET) {
			setErrored("Only GET is supported", Status.BAD_REQUEST);
			return root;
		}

		Guild g = WebsiteBot.getInstance().getGuild();

		List<JsonObject> objRoleList = new ArrayList<JsonObject>();


		for(int i = 0; i < g.getRoleCache().size(); i++) {

			Role role = g.getRoleCache().asList().get(i);

			if(role.getIdLong() == Roles.EVERYONE || 
					role.getIdLong() == Roles.SUSPICIOUS_ACCOUNT_REPORTER || 
					//					role.getName().contains("Bot") ||
					role.getIdLong() == Roles.ITS_MY_B_DAY || 
					role.getIdLong() == Roles.YOUTUBE_NOTIFICATIONS ||
					role.getIdLong() == Roles.MUTED ||
					//					role.getIdLong() == Roles.MEMBER ||
					role.getName().contains("Internal - ") || 
					role.getName().contains("Notifications") || 
					role.getName().equals("Server Captcha Bot")
					) {
				continue;
			}

			List<JsonObject> membersInRole = getMembers(g, role);
			
			if(membersInRole.isEmpty()) {
				continue;
			}
			
			JsonObject roleObj = new JsonObject();
			roleObj.addProperty("name", role.getName());

			Color roleColor = role.getColor();

			if(role.getIdLong() != Roles.MEMBER && (role.getColor() == null || role.getColorRaw() == Role.DEFAULT_COLOR_RAW)) {
				continue;
			}

			if(roleColor == null) {
				roleColor = new Color(Role.DEFAULT_COLOR_RAW);
			}


			String hex = "#"+Integer.toHexString(roleColor.getRGB()).substring(2).toUpperCase();
			roleObj.addProperty("color", hex);

			roleObj.add("members", ESSBot.GSON.toJsonTree(membersInRole));

			objRoleList.add(roleObj);

		}

		root.add("roles", ESSBot.GSON.toJsonTree(objRoleList));

		return root;
	}

	List<Long> duplateMembers = new ArrayList<Long>();
	private List<JsonObject> getMembers(Guild g, Role role) {
		List<JsonObject> list = new ArrayList<JsonObject>();
		List<Member> sorted = g.getMembersWithRoles(role);
		
		sorted.sort(new Comparator<Member>() {

			@Override
			public int compare(Member o1, Member o2) {
				return o1.getEffectiveName().compareTo(o2.getEffectiveName());
			}
			
		});
		
		for(Member m : sorted) {
			if(!duplateMembers.contains(m.getIdLong())) {
				list.add(toJson(m));
				duplateMembers.add(m.getIdLong());
			}

		}
		return list;
	}

	private static JsonObject toJson(Member member) {

		JsonObject obj = new JsonObject();
		obj.addProperty("displayName", member.getEffectiveName());
		obj.addProperty("actualName", member.getUser().getAsTag());
		obj.addProperty("avatar", member.getUser().getEffectiveAvatarUrl());
		//obj.addProperty("id", member.getId());

		return obj;
	}

}
