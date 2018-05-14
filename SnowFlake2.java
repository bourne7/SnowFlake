// https://github.com/bourne7/SnowFlake

public class SnowFlake2 {
    /**
     * 起始的时间戳。原本的值是 1480166465631。 实际上使用任何值都行。
     *
     * 按照原本的 12-5-5的配置：
     * 如果使用当前时间：比如2018年5月14号的时间：1526287619118L ，那么这个方法可以使用34年。
     * 如果使用0的话，估计用不了太久了。
     * 原因是long虽然有63位有效bit，但是其中只有41位（63-12-5-5=41）被使用了。
     *
     * 所以可以通过调节 BIT_SEQUENCE BIT_MACHINE BIT_DATACENTER 这3个值来弄成时间久一点的。
     * 不过看上去意义也不大。。时间同步这个问题不好解决。
     *
     * 如何将这个id弄短一点呢？可以通过将初试时间设置为 当前时间。将3个左移的值调小一点，尤其是
     * BIT_MACHINE 和 BIT_DATACENTER ，一般也没那么多数据中心。
     *
     */

    private final static long START_STAMP = 0L;

    /**
     * 每一部分占用的位数
     */
    //序列号占用的位数，默认值是 12，这个值是每毫秒生成的最大id数目。可以通过调节这3个值类适配。
    private final static long BIT_SEQUENCE = 12;
    //机器标识占用的位数，默认值是 5
    private final static long BIT_MACHINE = 5;
    //数据中心占用的位数，默认值是 5
    private final static long BIT_DATACENTER = 5;

    /**
     * 每一部分的最大值
     */
    private final static long MAX_SEQUENCE = -1L ^ (-1L << BIT_SEQUENCE);
//    private final static long MAX_MACHINE = -1L ^ (-1L << BIT_MACHINE);
//    private final static long MAX_DATACENTER = -1L ^ (-1L << BIT_DATACENTER);

    /**
     * 每一部分向左的位移
     */
    private final static long LEFT_MACHINE = BIT_SEQUENCE;
    private final static long LEFT_DATACENTER = BIT_MACHINE + BIT_SEQUENCE;
    private final static long LEFT_TIMESTAMP = BIT_DATACENTER + BIT_MACHINE + BIT_SEQUENCE;

    private static long dataCenterId = 7;      //数据中心，可以随便写。
    private static long machineId = 7;         //机器标识，可以随便写。和上面那个组合起来就成了物理上唯一了。
    private static long sequence = 0L;         //序列号
    private static long lastStamp = -1L;       //上一次时间戳

    /**
     * 产生下一个ID
     */
    public static synchronized long nextId() {
        long currStamp = System.currentTimeMillis();
        if (currStamp < lastStamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        if (currStamp == lastStamp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                // 这里就是同一个毫秒内，产生的不够用，就占用下一个毫秒的。
                currStamp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStamp = currStamp;
        //System.out.println((currStamp - START_STAMP) + " - currentTimeMillis");
        return (currStamp - START_STAMP) << LEFT_TIMESTAMP //时间戳部分
                | dataCenterId << LEFT_DATACENTER       //数据中心部分
                | machineId << LEFT_MACHINE             //机器标识部分
                | sequence;                             //序列号部分
    }

    /**
     * 这个方法主要是防止产生时间倒退。
     */
    private static long getNextMill() {
        long mill = System.currentTimeMillis();
        while (mill <= lastStamp) {
            mill = System.currentTimeMillis();
        }
        return mill;
    }

}
