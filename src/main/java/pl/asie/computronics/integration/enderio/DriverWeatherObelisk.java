package pl.asie.computronics.integration.enderio;

import crazypants.enderio.machine.weather.TileWeatherObelisk;
import crazypants.enderio.machine.weather.TileWeatherObelisk.WeatherTask;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import pl.asie.computronics.integration.CCMultiPeripheral;
import pl.asie.computronics.integration.ManagedEnvironmentOCTile;
import pl.asie.computronics.reference.Names;

import java.util.LinkedHashMap;

/**
 * @author Vexatos
 */
public class DriverWeatherObelisk {

	private static Object[] activate(TileWeatherObelisk tile, int taskID) {
		final WeatherTask[] VALUES = WeatherTask.values();
		taskID--;
		if(taskID != -1 && (taskID < 0 || taskID >= VALUES.length)) {
			return new Object[] { false, "invalid weather mode. needs to be between 1 and " + String.valueOf(VALUES.length) };
		}
		return new Object[] { tile.startTask(taskID) };
	}

	private static Object[] canActivate(TileWeatherObelisk tile, int taskID) {
		final WeatherTask[] VALUES = WeatherTask.values();
		taskID--;
		if(taskID != -1 && (taskID < 0 || taskID >= VALUES.length)) {
			return new Object[] { false, "invalid weather mode. needs to be -1 or between 1 and " + String.valueOf(VALUES.length) };
		}
		return new Object[] { tile.canStartTask(VALUES[taskID]) };
	}

	private static Object[] weather_modes() {
		LinkedHashMap<String, Integer> weatherModes = new LinkedHashMap<String, Integer>();
		final WeatherTask[] VALUES = WeatherTask.values();
		for(int i = 0; i < VALUES.length; i++) {
			weatherModes.put(VALUES[i].name(), i + 1);
		}
		return new Object[] { weatherModes };
	}

	public static class OCDriver extends DriverTileEntity {

		public class InternalManagedEnvironment extends ManagedEnvironmentOCTile<TileWeatherObelisk> {
			public InternalManagedEnvironment(TileWeatherObelisk tile) {
				super(tile, Names.EnderIO_WeatherObelisk);
			}

			@Override
			public int priority() {
				return 3;
			}

			@Callback(doc = "function(task:number):boolean; Returns true if the specified mode can currently be activated.")
			public Object[] canActivate(Context c, Arguments a) {
				return DriverWeatherObelisk.canActivate(tile, a.checkInteger(0));
			}

			@Callback(doc = "function(task:number):boolean; Tries to change the weather to the specified mode; Returns true on success")
			public Object[] activate(Context c, Arguments a) {
				return DriverWeatherObelisk.activate(tile, a.checkInteger(0));
			}

			@Callback(doc = "This is a table of all the availabe weather modes", getter = true)
			public Object[] weather_modes(Context c, Arguments a) {
				return DriverWeatherObelisk.weather_modes();
			}
		}

		@Override
		public Class<?> getTileEntityClass() {
			return TileWeatherObelisk.class;
		}

		@Override
		public ManagedEnvironment createEnvironment(World world, int x, int y, int z) {
			return new InternalManagedEnvironment(((TileWeatherObelisk) world.getTileEntity(x, y, z)));
		}
	}

	public static class CCDriver extends CCMultiPeripheral<TileWeatherObelisk> {

		public CCDriver() {
		}

		public CCDriver(TileWeatherObelisk tile, World world, int x, int y, int z) {
			super(tile, Names.EnderIO_WeatherObelisk, world, x, y, z);
		}

		@Override
		public int peripheralPriority() {
			return 3;
		}

		@Override
		public CCMultiPeripheral getPeripheral(World world, int x, int y, int z, int side) {
			TileEntity te = world.getTileEntity(x, y, z);
			if(te != null && te instanceof TileWeatherObelisk) {
				return new CCDriver((TileWeatherObelisk) te, world, x, y, z);
			}
			return null;
		}

		@Override
		public String[] getMethodNames() {
			return new String[] { "canActivate", "activate", "weather_modes" };
		}

		@Override
		public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
			switch(method) {
				case 0: {
					if(arguments.length < 1 || !(arguments[0] instanceof Double)) {
						throw new LuaException("first argument needs to be a number");
					}
					return DriverWeatherObelisk.canActivate(tile, ((Double) arguments[0]).intValue());
				}
				case 1: {
					if(arguments.length < 1 || !(arguments[0] instanceof Double)) {
						throw new LuaException("first argument needs to be a number");
					}
					return DriverWeatherObelisk.activate(tile, ((Double) arguments[0]).intValue());
				}
				case 2: {
					return DriverWeatherObelisk.weather_modes();
				}
			}
			return null;
		}
	}
}
