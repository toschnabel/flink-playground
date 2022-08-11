package playground

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.functions.{AssignerWithPeriodicWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows
import org.apache.flink.streaming.api.windowing.triggers.{Trigger, TriggerResult}
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow
import org.apache.flink.util.Collector

object TimestampTesting {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val stream = env.fromCollection(Seq[Int](1,2,3,4))

    val withTimestamp = stream

      .assignTimestampsAndWatermarks(
        new AssignerWithPeriodicWatermarks[Int] {
          override def getCurrentWatermark: Watermark = new Watermark(System.currentTimeMillis)
          override def extractTimestamp(element: Int, previousElementTimestamp: Long): Long = System.currentTimeMillis
        }
      )

      .process(
        (value: Int, ctx: ProcessFunction[Int, (Int, Long)]#Context, out: Collector[(Int, Long)]) => {
          out.collect((value, ctx.timestamp()))
        }
      )

    val nonWindowed = withTimestamp

    val windowed = withTimestamp
      .windowAll(GlobalWindows.create())
      .trigger(new Trigger[(Int, Long), GlobalWindow]() {
        override def onElement(element: (Int, Long), timestamp: Long, window: GlobalWindow, ctx: Trigger.TriggerContext): TriggerResult = TriggerResult.FIRE

        override def onProcessingTime(time: Long, window: GlobalWindow, ctx: Trigger.TriggerContext): TriggerResult = TriggerResult.FIRE

        override def onEventTime(time: Long, window: GlobalWindow, ctx: Trigger.TriggerContext): TriggerResult = TriggerResult.FIRE

        override def clear(window: GlobalWindow, ctx: Trigger.TriggerContext): Unit = {}

      })
      .aggregate(
        new AggregateFunction[(Int, Long), Int, (Int, Long)] {
          override def createAccumulator(): Int = 0

          override def add(value: (Int, Long), accumulator: Int): Int = accumulator + value._1

          override def getResult(accumulator: Int): (Int, Long) = (accumulator, -1)

          override def merge(a: Int, b: Int): Int = a + b
        }
      )

      // NOTE: This is needed to assign timestamps *after* going through the window
      .assignTimestampsAndWatermarks(
        new AssignerWithPeriodicWatermarks[(Int, Long)] {
          override def getCurrentWatermark: Watermark = new Watermark(System.currentTimeMillis)
          override def extractTimestamp(element: (Int, Long), previousElementTimestamp: Long): Long = System.currentTimeMillis
        }
      )

    nonWindowed.union(windowed)
      .process((value: (Int, Long), ctx: ProcessFunction[(Int, Long), (Int, Long, Long)]#Context, out: Collector[(Int, Long, Long)]) => {
        out.collect((value._1, value._2, ctx.timestamp()))
      })
      .print()

    env.execute
  }
}
