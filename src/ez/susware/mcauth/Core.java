package ez.susware.mcauth;

import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin implements Listener, CommandExecutor {

	public static ArrayList<Player> unverified = new ArrayList<Player>();
	
	public void onEnable() {
		this.getConfig().addDefault("players", 0);
		this.getConfig().options().copyDefaults(true);
		saveConfig();
		
        this.getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable() {
		saveConfig();
	}
	
	Core pl;
	FileConfiguration config = this.getConfig();
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		String path = "players." + p.getName().toLowerCase();
		unverified.add(p);
		if(!config.contains(path)) {
			p.sendMessage("§cRegister yourself with §7/reg (password) (password again)");
		} else {
			p.sendMessage("§cVerify yourself with §7/login (password)");
		}
	}
	
	@EventHandler 
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if(unverified.contains(p)) {
			p.teleport(e.getFrom());
			e.setCancelled(true);
		}
 	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player && unverified.contains(e.getEntity())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if(unverified.contains(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCmd(PlayerCommandPreprocessEvent e) {
		if(unverified.contains(e.getPlayer())) {
			if(e.getMessage().startsWith("/login") || e.getMessage().startsWith("/l") || e.getMessage().startsWith("/register") || e.getMessage().startsWith("/reg")) {
				e.setCancelled(false);
			} else {
				e.setCancelled(true);
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("login")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(!unverified.contains(p)) {
					p.sendMessage("§cYou've already logged in");
					return true;
				} else {
					if(args.length != 1) {
						p.sendMessage("§cUsage: /login (password)");
						return true;
					} else {
						String pw = config.getString("players." + p.getName().toLowerCase());
						if(args[0].equals(pw)) {
							p.sendMessage("§aSuccessfully logged in");
							unverified.remove(p);
						} else {
							p.sendMessage("§cWrong password!");
							return true;
						}
					}
				}
			}else {
				sender.sendMessage("wtf");
				return true;
			}
		}
		if(cmd.getName().equalsIgnoreCase("register")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				String path = "players." + p.getName().toLowerCase();
				if(config.contains(path)) {
					p.sendMessage("§cYou've already registered");
					return true;
				} else {
					if(!unverified.contains(p)) {
						p.sendMessage("§cYou've already logged in");
						return true;
					} else {
						if(args.length != 2) {
							p.sendMessage("§cUsage: /reg (password) (password again)");
							return true;
						} else {
							if(args[0].equals(args[1])) {
								p.sendMessage("§aSuccessfully registered!");
								config.set(path, args[0]);
								unverified.remove(p);
							} else {
								p.sendMessage("§cPasswords does not match");
								return true;
							}
						}
					}
				}
			} else {
				sender.sendMessage("wtf");
				return true;
			}
		}
		if(cmd.getName().equalsIgnoreCase("resetacc")) {
			if(sender instanceof Player) {
				sender.sendMessage("§cThis command is not for players");
				return true;
			} else {
				if(args.length == 1) {
					String path = "players." + args[0].toLowerCase();
					if(config.contains(path)) {
						config.set(path, null);
						sender.sendMessage("§aSuccessfully removed §7 " + args[0] + "§a's account");
						return true;
					} else {
						sender.sendMessage("§cThis player is not in the database");
						return true;
					}
				} else {
					sender.sendMessage("§cUsage: /resetacc (player)");
					return true;
				}
			}
		}
		if(cmd.getName().equalsIgnoreCase("changepass")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(args.length != 2) {
					p.sendMessage("§cUsage: /changepass (old password) (new password)");
					return true;
				} else {
					String pw = config.getString("players." + p.getName().toLowerCase());
					String path = "players." + p.getName().toLowerCase();
					if(args[0].equals(pw)) {
						config.set(path, args[1]);
						p.sendMessage("§aYour password successfully changed");
						return true;
					} else {
						p.sendMessage("§cYour old password does not match");
						return true;
					}
				}
			} else {
				sender.sendMessage("§c/resetacc jobb, hidd el");
				return true;
			}
		}
		return false;
	}
	
	
	
}
