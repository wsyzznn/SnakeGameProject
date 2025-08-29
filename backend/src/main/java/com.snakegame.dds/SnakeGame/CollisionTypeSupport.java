package com.snakegame.dds.SnakeGame;

import com.zrdds.infrastructure.*;
import com.zrdds.topic.TypeSupport;
import com.zrdds.publication.DataWriter;
import com.zrdds.subscription.DataReader;
import java.io.UnsupportedEncodingException;

public class CollisionTypeSupport extends TypeSupport {
    private String type_name = "Collision";
    private static TypeCodeImpl s_typeCode = null;
    private static CollisionTypeSupport m_instance = new CollisionTypeSupport();

    private final byte[] tmp_byte_obj = new byte[1];
    private final char[] tmp_char_obj = new char[1];
    private final short[] tmp_short_obj = new short[1];
    private final int[] tmp_int_obj = new int[1];
    private final long[] tmp_long_obj = new long[1];
    private final float[] tmp_float_obj = new float[1];
    private final double[] tmp_double_obj = new double[1];
    private final boolean[] tmp_boolean_obj = new boolean[1];

    
    private CollisionTypeSupport(){}

    
    public static TypeSupport get_instance() { return m_instance; }

    public Object create_sampleI() {
        Collision sample = new Collision();
        return sample;
    }

    public void destroy_sampleI(Object sample) {

    }

    public int copy_sampleI(Object dst,Object src) {
        Collision CollisionDst = (Collision)dst;
        Collision CollisionSrc = (Collision)src;
        CollisionDst.copy(CollisionSrc);
        return 1;
    }

    public int print_sample(Object _sample) {
        if (_sample == null){
            System.out.println("NULL");
            return -1;
        }
        Collision sample = (Collision)_sample;
        if (sample.player_id != null){
            System.out.println("sample.player_id:" + sample.player_id);
        }
        else{
            System.out.println("sample.player_id: null");
        }
        if (sample.collision_type != null){
            System.out.println("sample.collision_type:" + sample.collision_type);
        }
        else{
            System.out.println("sample.collision_type: null");
        }
        if (sample.target_id != null){
            System.out.println("sample.target_id:" + sample.target_id);
        }
        else{
            System.out.println("sample.target_id: null");
        }
        return 0;
    }

    public String get_type_name(){
        return this.type_name;
    }

    public int get_max_sizeI(){
        return 780;
    }

    public int get_max_key_sizeI(){
        return 780;
    }

    public boolean has_keyI(){
        return false;
    }

    public String get_keyhashI(Object sample, long cdr){
        return "-1";
    }

    public DataReader create_data_reader() {return new CollisionDataReader();}

    public DataWriter create_data_writer() {return new CollisionDataWriter();}

    public TypeCode get_inner_typecode(){
        TypeCode userTypeCode = get_typecode();
        if (userTypeCode == null) return null;
        return userTypeCode.get_impl();
    }

    public int get_sizeI(Object _sample,long cdr, int offset) throws UnsupportedEncodingException {
        int initialAlignment = offset;
        Collision sample = (Collision)_sample;
        offset += CDRSerializer.get_string_size(sample.player_id == null ? 0 : sample.player_id.getBytes().length, offset);

        offset += CDRSerializer.get_string_size(sample.collision_type == null ? 0 : sample.collision_type.getBytes().length, offset);

        offset += CDRSerializer.get_string_size(sample.target_id == null ? 0 : sample.target_id.getBytes().length, offset);

        return offset - initialAlignment;
    }

    public int serializeI(Object _sample ,long cdr) {
         Collision sample = (Collision) _sample;

        if (!CDRSerializer.put_string(cdr, sample.player_id, sample.player_id == null ? 0 : sample.player_id.length())){
            System.out.println("serialize sample.player_id failed.");
            return -2;
        }

        if (!CDRSerializer.put_string(cdr, sample.collision_type, sample.collision_type == null ? 0 : sample.collision_type.length())){
            System.out.println("serialize sample.collision_type failed.");
            return -2;
        }

        if (!CDRSerializer.put_string(cdr, sample.target_id, sample.target_id == null ? 0 : sample.target_id.length())){
            System.out.println("serialize sample.target_id failed.");
            return -2;
        }

        return 0;
    }

    synchronized public int deserializeI(Object _sample, long cdr){
        Collision sample = (Collision) _sample;
        sample.player_id = CDRDeserializer.get_string(cdr);
        if(sample.player_id ==null){
            System.out.println("deserialize member sample.player_id failed.");
            return -3;
        }

        sample.collision_type = CDRDeserializer.get_string(cdr);
        if(sample.collision_type ==null){
            System.out.println("deserialize member sample.collision_type failed.");
            return -3;
        }

        sample.target_id = CDRDeserializer.get_string(cdr);
        if(sample.target_id ==null){
            System.out.println("deserialize member sample.target_id failed.");
            return -3;
        }

        return 0;
    }

    public int get_key_sizeI(Object _sample,long cdr,int offset)throws UnsupportedEncodingException {
        int initialAlignment = offset;
        Collision sample = (Collision)_sample;
        offset += get_sizeI(sample, cdr, offset);
        return offset - initialAlignment;
    }

    public int serialize_keyI(Object _sample, long cdr){
        Collision sample = (Collision)_sample;
        return 0;
    }

    public int deserialize_keyI(Object _sample, long cdr) {
        Collision sample = (Collision)_sample;
        return 0;
    }

    public TypeCode get_typecode(){
        if (s_typeCode != null) {
            return s_typeCode;
        }
        TypeCodeFactory factory = TypeCodeFactory.get_instance();

        s_typeCode = factory.create_struct_TC("SnakeGame.Collision");
        if (s_typeCode == null){
            System.out.println("create struct Collision typecode failed.");
            return s_typeCode;
        }
        int ret = 0;
        TypeCodeImpl memberTc = new TypeCodeImpl();
        TypeCodeImpl eleTc = new TypeCodeImpl();

        memberTc = factory.create_string_TC(255);
        if (memberTc == null){
            System.out.println("Get Member player_id TypeCode failed.");
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }
        ret = s_typeCode.add_member_to_struct(
            0,
            0,
            "player_id",
            memberTc,
            false,
            false);
        factory.delete_TC(memberTc);
        if (ret < 0)
        {
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }

        memberTc = factory.create_string_TC(255);
        if (memberTc == null){
            System.out.println("Get Member collision_type TypeCode failed.");
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }
        ret = s_typeCode.add_member_to_struct(
            1,
            1,
            "collision_type",
            memberTc,
            false,
            false);
        factory.delete_TC(memberTc);
        if (ret < 0)
        {
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }

        memberTc = factory.create_string_TC(255);
        if (memberTc == null){
            System.out.println("Get Member target_id TypeCode failed.");
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }
        ret = s_typeCode.add_member_to_struct(
            2,
            2,
            "target_id",
            memberTc,
            false,
            false);
        factory.delete_TC(memberTc);
        if (ret < 0)
        {
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }

        return s_typeCode;
    }

}