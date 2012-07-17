package com.imdeity.deityapi.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.imdeity.deityapi.DeityAPI;
import com.imdeity.deityapi.DeityAPIConfigHelper;

/**
 * This class should be extended by the main JavaPlugin class
 * 
 * @author vanZeben
 */
public abstract class DeityPlugin extends JavaPlugin {
    
    public DeityPluginChat chat = null;
    public DeityPluginConfig config = null;
    public DeityPluginLanguage language = null;
    private ArrayList<Integer> taskList = new ArrayList<Integer>();
    public ArrayList<DeityListener> listeners = new ArrayList<DeityListener>();
    public ArrayList<DeityCommandHandler> commands = new ArrayList<DeityCommandHandler>();
    
    /**
     * The main bukkit onEnable. Calls all init methods
     */
    public void onEnable() {
        initPlugin();
        if (DeityAPI.plugin.config.getBoolean(DeityAPIConfigHelper.SHOULD_PROFILE)) {
            long startTime = System.currentTimeMillis();
            chat = new DeityPluginChat(getDescription().getName());
            config = new DeityPluginConfig(getDescription().getName(), this.getConfig(), "plugins/" + getDescription().getName() + "/config.yml");
            language = new DeityPluginLanguage(getDescription().getName(), this.getConfig(), "plugins/" + getDescription().getName() + "/language.yml");
            initConfig();
            config.saveConfig();
            initLanguage();
            language.save();
            
            initDatabase();
            initCmds();
            initListeners();
            initTasks();
            initInternalDatamembers();
            DeityAPI.registration.registerPlugin(this);
            long finalTime = System.currentTimeMillis();
            
            chat.out("Enabled - " + ((finalTime - startTime) / 1000) + (((finalTime - startTime) / 1000) == 1 ? " second" : " seconds"));
        } else {
            chat = new DeityPluginChat(getDescription().getName());
            config = new DeityPluginConfig(getDescription().getName(), this.getConfig(), "plugins/" + getDescription().getName() + "/config.yml");
            language = new DeityPluginLanguage(getDescription().getName(), this.getConfig(), "plugins/" + getDescription().getName() + "/language.yml");
            initConfig();
            config.saveConfig();
            initLanguage();
            language.save();
            
            initDatabase();
            initCmds();
            initListeners();
            initTasks();
            initInternalDatamembers();
            DeityAPI.registration.registerPlugin(this);
            chat.out("Enabled");
        }
    }
    
    /**
     * Main bukkit onDisable, Cancels all tasks registered
     */
    public void onDisable() {
        for (int i : taskList) {
            if (i != -1) {
                this.getServer().getScheduler().cancelTask(i);
            }
        }
        chat.out("Disabled");
    }
    
    /**
     * Second init called. Initializes the config with default nodes
     */
    protected abstract void initConfig();
    
    /**
     * Third init called. Initializes the language with default nodes
     */
    protected abstract void initLanguage();
    
    /**
     * Fifth init called. Initializes all DeityCommandHandler's
     */
    protected abstract void initCmds();
    
    /**
     * Fourth init called. Initializes any database tables or startup info
     */
    protected abstract void initDatabase();
    
    /**
     * Eighth init called. Initializes any further internal data-members if they
     * are otherwise un-initialized
     */
    protected abstract void initInternalDatamembers();
    
    /**
     * Sixth init called. Initializes all DeityListener's
     */
    protected abstract void initListeners();
    
    /**
     * Seventh init called. Initializes all tasks
     */
    protected abstract void initTasks();
    
    /**
     * First init called. Initializes the internal static DeityPlugin object, if
     * it exists
     */
    protected abstract void initPlugin();
    
    /**
     * Registers a DeityCommandHandler. To be called in the initCmds function
     * 
     * @param commandHandler
     */
    protected void registerCommand(DeityCommandHandler commandHandler) {
        this.commands.add(commandHandler);
        this.getCommand(commandHandler.getName()).setExecutor(commandHandler);
    }
    
    /**
     * Registers a DeityListener. To be called in the initListener function
     * 
     * @param listener
     */
    protected void registerListener(DeityListener listener) {
        this.listeners.add(listener);
        this.getServer().getPluginManager().registerEvents(listener, this);
    }
    
    /**
     * Configuration class
     * 
     * @author vanZeben
     */
    public class DeityPluginConfig {
        private FileConfiguration config = null;
        private File saveFile = null;
        private String pluginName;
        
        /**
         * Initializes the config from the main file
         * 
         * @param pluginName
         * @param config
         * @param saveLocation
         */
        public DeityPluginConfig(String pluginName, FileConfiguration config, String saveLocation) {
            this.pluginName = pluginName;
            Logger.getLogger("Minecraft").info("[" + pluginName + "] Loading Config...");
            this.config = config;
            saveFile = new File(saveLocation);
        }
        
        /**
         * Saves the config to the file passed in the constructor
         */
        public void saveConfig() {
            Logger.getLogger("Minecraft").info("[" + pluginName + "] Saving Config...");
            try {
                config.save(saveFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        /**
         * To be called in the DeityPlugin initConfig function. Initializes all
         * default config values
         * 
         * @param path
         * @param value
         */
        public void addDefaultConfigValue(String path, Object value) {
            if (!config.contains(path)) {
                if (value instanceof String) {
                    config.set(path, (String) value);
                } else {
                    config.set(path, value);
                }
            }
        }
        
        public boolean getBoolean(String path) {
            if (config.contains(path)) { return config.getBoolean(path); }
            return false;
        }
        
        public List<Boolean> getBooleanList(String path) {
            if (!config.contains(path)) { return config.getBooleanList(path); }
            return null;
        }
        
        public double getDouble(String path) {
            if (config.contains(path)) { return config.getDouble(path); }
            return 0;
        }
        
        public int getInt(String path) {
            if (config.contains(path)) { return config.getInt(path); }
            return 0;
        }
        
        public ItemStack getItemStack(String path) {
            if (config.contains(path)) { return config.getItemStack(path); }
            return null;
        }
        
        public long getLong(String path) {
            if (config.contains(path)) { return config.getLong(path); }
            return 0;
        }
        
        public String getString(String path) {
            if (config.contains(path)) { return config.getString(path); }
            return null;
        }
        
        public void set(String node, Object value) {
            config.set(node, value);
            this.saveConfig();
        }
    }
    
    /**
     * Language class
     * 
     * @author vanZeben
     */
    public class DeityPluginLanguage {
        private FileConfiguration language = null;
        private File saveFile = null;
        private String pluginName;
        
        /**
         * Initializes the language from the main file
         * 
         * @param pluginName
         * @param language
         * @param saveLocation
         */
        public DeityPluginLanguage(String pluginName, FileConfiguration language, String saveLocation) {
            this.pluginName = pluginName;
            Logger.getLogger("Minecraft").info("[" + pluginName + "] Loading Language...");
            this.language = language;
            saveFile = new File(saveLocation);
        }
        
        /**
         * Saves the langauge to the file passed in the constructor
         */
        public void save() {
            Logger.getLogger("Minecraft").info("[" + pluginName + "] Saving Language...");
            try {
                language.save(saveFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        /**
         * To be called in the DeityPlugin initLanguage function. Initializes
         * all default language values
         * 
         * @param path
         * @param value
         */
        public void addDefaultLanguageValue(String path, String value) {
            if (!language.contains(path)) {
                language.set(path, value);
            }
        }
        
        /**
         * Returns a language node
         * 
         * @param node
         * @return
         */
        public String getNode(String node) {
            if (language.contains(node)) { return language.getString(node); }
            return null;
        }
        
        /**
         * Saves a language node
         * 
         * @param node
         * @param value
         */
        public void setNode(String node, String value) {
            language.set(node, value);
            this.save();
        }
    }
    
    /**
     * Default plugin chat. Calls the ChatAPI, but uses the plugin name as the
     * header
     * 
     * @author vanZeben
     */
    public class DeityPluginChat {
        private String pluginName = "";
        
        public DeityPluginChat(String pluginName) {
            this.pluginName = pluginName;
        }
        
        /**
         * Sends a message to console
         * 
         * @param msg
         */
        public void out(String msg) {
            DeityAPI.getAPI().getChatAPI().out(pluginName, msg);
        }
        
        /**
         * Sends a warning message to console
         * 
         * @param msg
         */
        public void outWarn(String msg) {
            DeityAPI.getAPI().getChatAPI().outWarn(pluginName, msg);
        }
        
        /**
         * Sends a severe message to console
         * 
         * @param msg
         */
        public void outSevere(String msg) {
            DeityAPI.getAPI().getChatAPI().outSevere(pluginName, msg);
        }
        
        /**
         * Sends a player message
         * 
         * @param player
         * @param msg
         */
        public void sendPlayerMessage(Player player, String msg) {
            DeityAPI.getAPI().getChatAPI().sendPlayerMessage(player, pluginName, msg);
        }
        
        /**
         * Sends a player message with no header
         * 
         * @param player
         * @param msg
         */
        public void sendPlayerMessageNoHeader(Player player, String msg) {
            DeityAPI.getAPI().getChatAPI().sendPlayerMessageNoTitleNewLine(player, msg);
        }
        
        /**
         * Sends a message to all online
         * 
         * @param msg
         */
        public void sendGlobalMessage(String msg) {
            DeityAPI.getAPI().getChatAPI().sendGlobalMessage(pluginName, msg);
        }
        
        /**
         * Sends a message to all online with no header
         * 
         * @param msg
         */
        public void sendGlobalNoHeader(String msg) {
            DeityAPI.getAPI().getChatAPI().sendGlobalMessage("", msg);
        }
    }
}