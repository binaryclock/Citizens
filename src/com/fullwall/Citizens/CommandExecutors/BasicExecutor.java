package com.fullwall.Citizens.CommandExecutors;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.fullwall.Citizens.Citizens;
import com.fullwall.Citizens.Permission;
import com.fullwall.Citizens.Economy.EconomyHandler;
import com.fullwall.Citizens.Economy.EconomyHandler.Operation;
import com.fullwall.Citizens.NPCs.NPCManager;
import com.fullwall.Citizens.Utils.MessageUtils;
import com.fullwall.Citizens.Utils.PropertyPool;
import com.fullwall.Citizens.Utils.StringUtils;
import com.fullwall.resources.redecouverte.NPClib.HumanNPC;

public class BasicExecutor implements CommandExecutor {

	private Citizens plugin;

	public BasicExecutor(Citizens plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(MessageUtils.mustBeIngameMessage);
			return true;
		}

		Player player = (Player) sender;
		HumanNPC npc = null;
		// Get the selected NPC, if any (required for most commands)
		if (NPCManager.validateSelected(player))
			npc = NPCManager
					.getNPC(NPCManager.NPCSelected.get(player.getName()));

		if (args.length >= 2 && args[0].equals("create")) {
			if (hasPermission("citizens.basic.create", sender)) {
				if (!EconomyHandler.useEconomy()
						|| EconomyHandler.canBuy(Operation.BASIC_NPC_CREATE,
								player)) {
					createNPC(args, player);
				} else if (EconomyHandler.useEconomy()) {
					sender.sendMessage(MessageUtils.getNoMoneyMessage(
							Operation.BASIC_NPC_CREATE, player));
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length == 1 && (args[0].equals("move"))) {
			if (hasPermission("citizens.general.move", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.general.move")) {
						moveNPC(sender, npc.getName(),
								Integer.valueOf(NPCManager.NPCSelected
										.get(player.getName())));
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if ((args.length == 1 || args.length == 2)
				&& args[0].equals("remove")) {
			if (hasPermission("citizens.general.remove.singular", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.remove.singular")) {
						removeNPC(args, sender);
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else if (hasPermission("citizens.general.remove.all", sender)) {
				if (args.length == 2 && args[1].equals("all")) {
					removeNPC(args, sender);
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length == 2 && args[0].equals("name")) {
			if (hasPermission("citizens.general.setname", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.general.setname")) {
						setName(args, sender);
						NPCManager.NPCSelected.remove(player.getName());
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length == 2
				&& (args[0].equals("colour") || args[0].equals("color"))) {
			if (hasPermission("citizens.general.colour", sender)
					|| hasPermission("citizens.general.color", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.general.colour")
							|| NPCManager.validateOwnership(npc.getUID(),
									player, "citizens.general.color")) {
						setColour(args, player);
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length >= 2 && args[0].equals("add")) {
			if (hasPermission("citizens.basic.settext", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.basic.settext")) {
						addText(args, sender);
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length >= 2 && args[0].equals("set")) {
			if (hasPermission("citizens.basic.settext", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.basic.settext")) {
						setText(args, sender);
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length == 1 && (args[0].equals("reset"))) {
			if (hasPermission("citizens.basic.settext", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.basic.settext")) {
						resetText(args, sender);
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length == 2 && (args[0].equals("item"))) {
			if (hasPermission("citizens.general.setitem", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.general.setitem")) {
						setItemInHand(sender, args);
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length == 2
				&& (args[0].equals("torso") || args[0].equals("legs")
						|| args[0].equals("helmet") || args[0].equals("boots"))) {
			if (hasPermission("citizens.general.setitem", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.general.setitem")) {
						setArmor(sender, args);
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length >= 1 && args[0].equals("tp")) {
			if (hasPermission("citizens.general.tp", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.general.tp")) {
						player.teleport((PropertyPool.getLocationFromID(npc
								.getUID())));
						sender.sendMessage(ChatColor.GREEN
								+ "Teleported you to the NPC named "
								+ StringUtils.yellowify(npc.getSpacedName(),
										ChatColor.GREEN) + ". Enjoy!");
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if ((command.getName().equals("citizens") || command.getName()
				.equals("npc")) && args.length == 1 && (args[0].equals("help"))) {
			if (hasPermission("citizens.help", sender)) {
				sendHelp(sender);
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if ((command.getName().equals("citizens") || command.getName()
				.equals("npc")) && args.length == 2 && (args[0].equals("help"))) {
			if (hasPermission("citizens.help", sender)) {
				sendHelpPage(sender, args[1]);
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length == 1 && (args[0].equals("copy"))) {
			if (hasPermission("citizens.general.copy", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.general.copy")) {
						copyNPC(npc, player);
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length == 1 && (args[0].equals("getid"))) {
			if (hasPermission("citizens.general.getid", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.general.getid")) {
						player.sendMessage(ChatColor.GREEN
								+ "The ID of this NPC is: "
								+ StringUtils.yellowify("" + npc.getUID(),
										ChatColor.GREEN) + ".");
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length == 2 && (args[0].equals("select"))) {
			if (hasPermission("citizens.general.select", sender)) {
				// BUILD CHECK
				if (!Character.isDigit(args[1].charAt(0))) {
					player.sendMessage(ChatColor.RED
							+ "The ID must be a number.");
					return true;
				}
				npc = NPCManager.getNPC(Integer.valueOf(args[1]));
				if (npc == null) {
					sender.sendMessage(ChatColor.RED + "No NPC with the ID "
							+ args[1] + ".");
				} else {
					NPCManager.NPCSelected.put(player.getName(), npc.getUID());
					player.sendMessage(ChatColor.GREEN
							+ "Selected NPC with ID: "
							+ StringUtils.yellowify("" + npc.getUID(),
									ChatColor.GREEN)
							+ " Name: "
							+ StringUtils.yellowify(npc.getSpacedName(),
									ChatColor.GREEN) + ".");
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length == 1 && (args[0].equals("getowner"))) {
			if (hasPermission("citizens.general.getowner", sender)) {
				if (npc != null) {
					player.sendMessage(ChatColor.GREEN
							+ "The owner of this NPC is: "
							+ StringUtils.yellowify(
									PropertyPool.getNPCOwner(npc.getUID()),
									ChatColor.GREEN) + ".");
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length == 2 && (args[0].equals("setowner"))) {
			if (hasPermission("citizens.general.setowner", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.general.setowner")) {
						PropertyPool.setNPCOwner(npc.getUID(), args[1]);
						player.sendMessage(ChatColor.GREEN
								+ "The owner of NPC: �c"
								+ StringUtils.yellowify(npc.getSpacedName(),
										ChatColor.GREEN)
								+ " is now: "
								+ StringUtils.yellowify(args[1],
										ChatColor.GREEN) + ".");
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length == 2 && (args[0].equals("addowner"))) {
			if (hasPermission("citizens.general.addowner", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.general.addowner")) {
						PropertyPool.addNPCOwner(npc.getUID(), args[1], player);
						player.sendMessage(ChatColor.GREEN
								+ "Added "
								+ StringUtils.yellowify(args[1],
										ChatColor.GREEN)
								+ " to the owner list of "
								+ StringUtils.yellowify(npc.getSpacedName(),
										ChatColor.GREEN) + ".");
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length == 2 && (args[0].equals("talkwhenclose"))) {
			if (hasPermission("citizens.general.talkwhenclose", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.general.talkwhenclose")) {
						changeTalkWhenClose(player, args[1]);
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else if (args.length == 2 && (args[0].equals("lookatplayers"))) {
			if (hasPermission("citizens.general.lookatplayers", sender)) {
				if (npc != null) {
					if (NPCManager.validateOwnership(npc.getUID(), player,
							"citizens.general.lookatplayers")) {
						changeLookWhenClose(player, args[1]);
					} else {
						sender.sendMessage(MessageUtils.notOwnerMessage);
					}
				} else {
					sender.sendMessage(MessageUtils.mustHaveNPCSelectedMessage);
				}
			} else {
				sender.sendMessage(MessageUtils.noPermissionsMessage);
			}
			return true;

		} else {
			if (args.length >= 2 && args[0].equals("move"))
				sender.sendMessage(ChatColor.RED
						+ "Incorrect Syntax: /npc move");
			else if (args.length >= 3 && args[0].equals("remove"))
				sender.sendMessage(ChatColor.RED
						+ "Incorrect Syntax: /npc remove OR /npc remove all");
			else if (args.length >= 3 && args[0].equals("name"))
				sender.sendMessage(ChatColor.RED
						+ "Incorrect Syntax: /npc name [name]");
			else if (args.length >= 3
					&& (args[0].equals("colour") || args[0].equals("color")))
				sender.sendMessage(ChatColor.RED
						+ "Incorrect Syntax: /npc color [color]");
			else if (args.length >= 2 && (args[0].equals("reset")))
				sender.sendMessage(ChatColor.RED
						+ "Incorrect Syntax: /npc reset");
			else if (args.length >= 3 && (args[0].equals("item")))
				sender.sendMessage(ChatColor.RED
						+ "Incorrect Syntax: /npc item [id|item name]");
			else if (args.length >= 3
					&& (args[0].equals("torso") || args[0].equals("legs")
							|| args[0].equals("helmet") || args[0]
							.equals("boots")))
				sender.sendMessage(ChatColor.RED + "Incorrect Syntax: /npc "
						+ args[0] + " [id|item name]");
			else if (args.length >= 2 && args[0].equals("tp"))
				sender.sendMessage(ChatColor.RED + "Incorrect Syntax: /npc tp");
			else if ((command.getName().equals("citizens") || command.getName()
					.equals("npc"))
					&& args.length >= 3
					&& (args[0].equals("help")))
				sender.sendMessage(ChatColor.RED
						+ "Incorrect Syntax: /npc help [page number]");
			else if (args.length >= 2 && (args[0].equals("copy")))
				sender.sendMessage(ChatColor.RED
						+ "Incorrect Syntax: /npc copy");
			else if (args.length >= 2 && (args[0].equals("getid")))
				sender.sendMessage(ChatColor.RED
						+ "Incorrect Syntax: /npc getid");
			else if (args.length >= 3 && (args[0].equals("select")))
				sender.sendMessage(ChatColor.RED
						+ "Incorrect Syntax: /npc select [id]");
			else if (args.length >= 2 && (args[0].equals("getowner")))
				sender.sendMessage(ChatColor.RED
						+ "Incorrect Syntax: /npc getowner");
			else if (args.length >= 3 && (args[0].equals("setowner")))
				sender.sendMessage(ChatColor.RED
						+ "Incorrect Syntax: /npc setowner [player name]");
			else if (args.length >= 3 && (args[0].equals("addowner")))
				sender.sendMessage(ChatColor.RED
						+ "Incorrect Syntax: /npc addowner [player name]");
			else if (args.length >= 3 && (args[0].equals("talkwhenclose")))
				sender.sendMessage(ChatColor.RED
						+ "Incorrect Syntax: /npc talkwhenclose [true|false]");
			else if (args.length >= 3 && (args[0].equals("lookatplayers")))
				sender.sendMessage(ChatColor.RED
						+ "Incorrect Syntax: /npc lookatplayers [true|false]");
			else
				return false;

			return true;
		}
	}

	private void createNPC(String[] args, Player player) {
		String text = "";
		ArrayList<String> texts = new ArrayList<String>();
		if (args.length >= 3) {
			int i = 0;
			for (String s : args) {
				if (i == 2 && !s.isEmpty() && !s.equals(";"))
					text += s;
				if (i > 2 && !s.isEmpty() && !s.equals(";"))
					text += " " + s;
				i += 1;
			}
			texts.add(text);
		}
		int UID = plugin.handler.spawnNPC(args[1], player.getLocation());
		plugin.handler.setNPCText(UID, texts);
		plugin.handler.setOwner(UID, player.getName());
		player.sendMessage(ChatColor.GREEN + "The NPC "
				+ StringUtils.yellowify(args[1], ChatColor.GREEN)
				+ " was born!");
		if (EconomyHandler.useEconomy()) {
			int paid = EconomyHandler.pay(Operation.BASIC_NPC_CREATE, player);
			if (paid > 0)
				player.sendMessage(MessageUtils.getPaidMessage(
						Operation.BASIC_NPC_CREATE, paid, args[1], "", false));
		}
		NPCManager.NPCSelected.put(player.getName(), UID);
		player.sendMessage(ChatColor.GREEN + "You selected NPC ["
				+ StringUtils.yellowify(args[1], ChatColor.GREEN) + "], ID ["
				+ StringUtils.yellowify("" + UID, ChatColor.GREEN) + "]");
	}

	private void moveNPC(CommandSender sender, String name, int UID) {
		Location loc = PropertyPool.getLocationFromID(UID);
		if (loc != null) {
			PropertyPool.saveLocation(name, loc, UID);
		}
		plugin.handler.moveNPC(UID, ((Player) sender).getLocation());
		sender.sendMessage(StringUtils.yellowify(name, ChatColor.GREEN)
				+ " is enroute to your location!");
	}

	private void removeNPC(String[] args, CommandSender sender) {
		Player p = (Player) sender;
		if (args.length == 2 && args[1].equals("all")) {
			plugin.handler.removeAllNPCs();
			sender.sendMessage(ChatColor.GRAY + "The NPC(s) disappeared.");
			PropertyPool.locations.setInt("currentID", 0);
			PropertyPool.locations.removeKey("list");
		} else {
			HumanNPC n = NPCManager.getNPC(NPCManager.NPCSelected.get(p
					.getName()));
			plugin.handler.removeNPC(Integer.valueOf(NPCManager.NPCSelected
					.get(p.getName())));
			sender.sendMessage(ChatColor.GRAY + n.getName() + " disappeared.");
		}
		NPCManager.NPCSelected.remove(p.getName());
	}

	private void setName(String[] args, CommandSender sender) {
		Player p = (Player) sender;
		HumanNPC n = NPCManager.getNPC(NPCManager.NPCSelected.get(p.getName()));
		plugin.handler.setName(n.getUID(), args[1]);
		sender.sendMessage(ChatColor.GREEN + n.getName()
				+ "'s name was set to " + args[1] + ".");
		return;
	}

	private void setColour(String[] args, Player p) {
		if (args[1].indexOf('&') != 0) {
			p.sendMessage(ChatColor.GRAY + "Use an & to specify " + args[0]
					+ ".");
		} else {
			HumanNPC n = NPCManager.getNPC(NPCManager.NPCSelected.get(p
					.getName()));
			plugin.handler.setColour(n.getUID(), args[1]);
		}
	}

	private void setText(String[] args, CommandSender sender) {
		Player p = (Player) sender;
		HumanNPC n = NPCManager.getNPC(NPCManager.NPCSelected.get(p.getName()));
		String text = "";
		if (args.length >= 2) {
			int i = 0;
			for (String s : args) {
				if (i == 1 && !s.isEmpty() && !s.equals(";"))
					text += s;
				if (i > 1 && !s.isEmpty() && !s.equals(";"))
					text += " " + s;
				i += 1;
			}
		}
		ArrayList<String> texts = new ArrayList<String>();
		texts.add(text);
		plugin.handler.setNPCText(n.getUID(), texts);
		sender.sendMessage(ChatColor.GREEN + n.getName()
				+ "'s text was set to " + text);

	}

	private void addText(String[] args, CommandSender sender) {
		Player p = (Player) sender;
		HumanNPC n = NPCManager.getNPC(NPCManager.NPCSelected.get(p.getName()));
		String text = "";
		int i = 0;
		for (String s : args) {
			if (i == 1 && !s.isEmpty() && !s.equals(";")) {
				text += s;
			}
			if (i > 1 && !s.isEmpty() && !s.equals(";")) {
				text += " " + s;
			}
			i += 1;
		}
		plugin.handler.addNPCText(n.getUID(), text);
		sender.sendMessage(ChatColor.GREEN
				+ text
				+ " was added to "
				+ StringUtils.yellowify(n.getSpacedName() + "'s",
						ChatColor.GREEN) + " text.");
	}

	private void resetText(String[] args, CommandSender sender) {
		Player p = (Player) sender;
		HumanNPC n = NPCManager.getNPC(NPCManager.NPCSelected.get(p.getName()));
		plugin.handler.resetText(n.getUID());
		sender.sendMessage(StringUtils.yellowify(n.getSpacedName() + "'s",
				ChatColor.GREEN) + " text was reset!");
	}

	// TODO - should pull from player's inventory to prevent misuse.
	private void setItemInHand(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		HumanNPC n = NPCManager.getNPC(NPCManager.NPCSelected.get(p.getName()));
		plugin.handler.setItemInHand(p, n.getUID(), args[1]);
	}

	// TODO - see above.
	private void setArmor(CommandSender sender, String[] args) {
		plugin.handler.setItemInSlot((Player) sender, args);
	}

	// Ugh, ugly. Maybe we should palm the PropertyPool functions off to
	// somewhere else.
	private void copyNPC(HumanNPC NPC, Player p) {
		ArrayList<String> texts = PropertyPool.getText(NPC.getUID());
		String colour = PropertyPool.getColour(NPC.getUID());
		String owner = PropertyPool.getNPCOwner(NPC.getUID());
		ArrayList<Integer> items = PropertyPool.getItems(NPC.getUID());
		boolean lookatplayers = PropertyPool.getNPCLookWhenClose(NPC.getUID());
		boolean talkwhenclose = PropertyPool.getNPCTalkWhenClose(NPC.getUID());
		int newUID = plugin.handler.spawnNPC(NPC.getName(), p.getLocation());

		HumanNPC newNPC = NPCManager.getNPC(newUID);

		PropertyPool.saveColour(newUID, colour);
		PropertyPool.saveText(newUID, texts);
		PropertyPool.saveItems(newUID, items);
		PropertyPool.saveLookWhenClose(newUID, lookatplayers);
		PropertyPool.saveTalkWhenClose(newUID, talkwhenclose);
		PropertyPool.setNPCOwner(newUID, owner);

		// NPCDataManager.addItems(newNPC, items);

		String name = newNPC.getName();
		NPCManager.removeNPCForRespawn(newUID);
		plugin.handler.spawnExistingNPC(name, newUID);
	}

	private void changeTalkWhenClose(Player p, String bool) {
		boolean talk = false;
		if (bool.equals("true"))
			talk = true;
		HumanNPC n = NPCManager.getNPC(NPCManager.NPCSelected.get(p.getName()));
		PropertyPool.setNPCTalkWhenClose(n.getUID(), talk);
		if (talk)
			p.sendMessage(ChatColor.GREEN + "The NPC: "
					+ StringUtils.yellowify(n.getSpacedName(), ChatColor.GREEN)
					+ " will now talk to nearby players.");
		else if (!talk)
			p.sendMessage(ChatColor.GREEN + "The NPC: "
					+ StringUtils.yellowify(n.getSpacedName(), ChatColor.GREEN)
					+ " will stop talking to nearby players.");
	}

	private void changeLookWhenClose(Player p, String bool) {
		boolean look = false;
		if (bool.equals("true"))
			look = true;
		HumanNPC n = NPCManager.getNPC(NPCManager.NPCSelected.get(p.getName()));
		PropertyPool.setNPCLookWhenClose(n.getUID(), look);
		if (look)
			p.sendMessage(ChatColor.GREEN + "The NPC: "
					+ StringUtils.yellowify(n.getSpacedName(), ChatColor.GREEN)
					+ " will now look at players.");
		else if (!look)
			p.sendMessage(ChatColor.GREEN + "The NPC: "
					+ StringUtils.yellowify(n.getSpacedName(), ChatColor.GREEN)
					+ " will stop looking at players.");
	}

	private void sendHelp(CommandSender sender) {
		// remove, reset, add, color
		sender.sendMessage("�fCitizens v1.07 Help");
		sender.sendMessage("�b-------------------------------");
		sender.sendMessage("�8/�cnpc �bcreate (text) �e- �acreates an NPC at your location.");
		sender.sendMessage("�8/�cnpc �bset [text] �e- �asets the text of an NPC.");
		sender.sendMessage("�8/�cnpc �badd [text] �e- �aadds text to an NPC.");
		sender.sendMessage("�fUse the command /citizens help [page] for more info.");

	}

	private void sendHelpPage(CommandSender sender, String page) {
		int pageNum = Integer.valueOf(page);
		switch (pageNum) {
		case 1:
			sender.sendMessage("�fCitizens v1.07 Help");
			sender.sendMessage("�b-------------------------------");
			sender.sendMessage("�8/�cnpc �bcreate (text) �e- �acreates an NPC at your location.");
			sender.sendMessage("�8/�cnpc �bset [text] �e- �asets the text of an NPC.");
			sender.sendMessage("�8/�cnpc �badd [text] �e- �aadds text to an NPC.");
			sender.sendMessage("�fUse the command /citizens help [page] for more info.");
			break;
		case 2:
			sender.sendMessage("�8/�cnpc �bname [new name] �e- �achanges the name of an NPC.");
			sender.sendMessage("�8/�cnpc �bremove [all] �e- �adeletes and despawns the NPC(s).");
			sender.sendMessage("�8/�cnpc �breset �e- �aresets the messages of an NPC.");
			sender.sendMessage("�8/�cnpc �bcolo(u)r [&(code)] �e- �aedits the color of an NPC's name.");
			sender.sendMessage("�8/�cnpc �bitem [id|item name] �e- �asets the in-hand item of an NPC.");
			sender.sendMessage("�8/�cnpc �bhelmet|torso|legs|boots [id|item name] �e- �asets the item slot of an NPC.");
			break;
		case 3:
			sender.sendMessage("�8/�cnpc �bmove �e- �amoves an NPC to your location.");
			sender.sendMessage("�8/�cnpc �btp �e- �ateleports you to the location of an NPC.");
			sender.sendMessage("�8/�cnpc �bcopy �e- �amakes of copy of the NPC on your location.");
			sender.sendMessage("�8/�cnpc �bgetid �e- �agets the ID of the selected NPC.");
			sender.sendMessage("�8/�cnpc �bselect [id] �e- �aselects an NPC with the given ID.");
			sender.sendMessage("�8/�cnpc �bgetowner �e- �agets the owner of the selected NPC.");
			break;
		case 4:
			sender.sendMessage("�8/�cnpc �bsetowner [name] �e- �asets the owner of the selected NPC.");
			sender.sendMessage("�8/�cnpc �btalkwhenclose [true|false] �e- �amake a NPC talk to players.");
			sender.sendMessage("�8/�cnpc �blookatplayers [true|false] �e- �amake a NPC look at players.");
			sender.sendMessage("�b-------------------------------");
			sender.sendMessage("�fPlugin made by fullwall, NeonMaster and TheMPC.");
			break;
		default:
			sender.sendMessage("�fThe total number of pages is 4, page: "
					+ pageNum + " is not available.");
			break;
		}
	}

	public boolean validateUID(int UID, CommandSender sender) {
		if (!plugin.validateUID(UID)) {
			sender.sendMessage(ChatColor.GRAY
					+ "Couldn't find the NPC with id " + UID + ".");
			return false;
		}
		return true;
	}

	public static boolean hasPermission(String permission, CommandSender sender) {
		return (!(sender instanceof Player) || Permission.generic(
				(Player) sender, permission));
	}
}
