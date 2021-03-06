package ds.mods.OCLights2.serialize;

import java.util.Map;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import ds.mods.OCLights2.gpu.GPU;

public class Serialize {
	public static Object[][] classToSerializer  = new Object[][]
			{
		{Byte.class,new Numbers.ByteSerializer()},
		{Short.class,new Numbers.ShortSerializer()},
		{Integer.class,new Numbers.IntegerSerializer()},
		{Float.class,new Numbers.FloatSerializer()},
		{Double.class,new Numbers.DoubleSerializer()},
		{Long.class,new Numbers.LongSerializer()},
		{Map.class,new MapSerializer()},
		{GPU.class,new GPUSerializer()},
		{String.class,new StringSerializer()},
		{Character.class, new Numbers.CharacterSerializer()},
			};

	public static void serialize(ByteArrayDataOutput dat, Object o)
	{
		if(o != null){
			Class<?> clazz = o.getClass();
			int i = 0;
			for (Object[] ol : classToSerializer)
			{
				Class<?> cla = (Class<?>) ol[0];
				if (cla.equals(clazz))
				{
					dat.writeByte(i);
					ISerializer s = (ISerializer) ol[1];
					s.write(o, dat);
					return;
				}
				i++;
			}
			throw new IllegalArgumentException(clazz.getName()+" is not serializable!");
		}
	}

	public static Object unserialize(ByteArrayDataInput dat)
	{
		int typ = dat.readByte();
		ISerializer s = (ISerializer) classToSerializer[typ][1];
		return s.read(dat);
	}
}
