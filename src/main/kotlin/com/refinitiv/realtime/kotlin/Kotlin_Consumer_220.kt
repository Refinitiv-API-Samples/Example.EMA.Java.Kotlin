//|-----------------------------------------------------------------------------
//|            This source code is provided under the Apache 2.0 license      --
//|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
//|                See the project's LICENSE.md for details.                  --
//|            Copyright (C) 2017-2020 Refinitiv. All rights reserved.        --
//|-----------------------------------------------------------------------------

package com.refinitiv.realtime.kotlin

import kotlin.collections.Iterator
import com.refinitiv.ema.access.Msg
import com.refinitiv.ema.access.AckMsg
import com.refinitiv.ema.access.GenericMsg
import com.refinitiv.ema.access.RefreshMsg
import com.refinitiv.ema.access.StatusMsg
import com.refinitiv.ema.access.UpdateMsg
import com.refinitiv.ema.access.Data
import com.refinitiv.ema.access.DataType
import com.refinitiv.ema.access.DataType.DataTypes
import com.refinitiv.ema.access.EmaFactory
import com.refinitiv.ema.access.FieldEntry
import com.refinitiv.ema.access.FieldList
import com.refinitiv.ema.access.OmmConsumer
import com.refinitiv.ema.access.OmmConsumerClient
import com.refinitiv.ema.access.OmmConsumerEvent
import com.refinitiv.ema.access.OmmException

//Client class, implements OmmConsumerClient interface
class AppclientFieldListWalk : OmmConsumerClient {

    override fun onRefreshMsg(refreshMsg: RefreshMsg, event: OmmConsumerEvent): Unit {

        if (refreshMsg.hasName()) println("Refresh: Item Name: ${refreshMsg.name()}")

        if (refreshMsg.hasServiceName()) println("Refresh: Service Name: ${refreshMsg.serviceName()}")

        println("Refresh:  Item State: ${refreshMsg.state()}")

        if(DataType.DataTypes.FIELD_LIST == refreshMsg.payload().dataType()) decode(refreshMsg.payload().fieldList())

        println("")
    }

    override fun onUpdateMsg(updateMsg: UpdateMsg, event: OmmConsumerEvent): Unit {

        if (updateMsg.hasName()) println("Update: Item Name: ${updateMsg.name()}")

        if (updateMsg.hasServiceName()) println("Update: Service Name: ${updateMsg.serviceName()}")

        if(DataType.DataTypes.FIELD_LIST == updateMsg.payload().dataType()) decode(updateMsg.payload().fieldList())

        println("")
    }

    override fun onStatusMsg(statusMsg: StatusMsg, event: OmmConsumerEvent): Unit {

        if (statusMsg.hasName()) println("Status: Item Name: ${statusMsg.name()}")

        if (statusMsg.hasServiceName()) println("Status: Service Name: ${statusMsg.serviceName()}")

        if(statusMsg.hasState()) println("Status: Item State: ${statusMsg.state()}")
    }

    override fun onGenericMsg(genericMsg: GenericMsg, event: OmmConsumerEvent): Unit {}

    override fun onAckMsg(ackMsg: AckMsg, event: OmmConsumerEvent): Unit {}

    override fun onAllMsg(msg: Msg, event: OmmConsumerEvent): Unit {}

    //Iterates OMM FieldList, then parse each OMM FieldEntry based on FID type
    fun decode(fieldList: FieldList): Unit {
        for (fieldEntry: FieldEntry in fieldList) {
            print("Fid: ${fieldEntry.fieldId()} Name = ${fieldEntry.name()} DataType: ${DataType.asString(fieldEntry.load().dataType())} Value: ")

            if (fieldEntry.code() == Data.DataCode.BLANK) {
                println(" blank")
            } else {
                when (fieldEntry.loadType()) {
                    DataTypes.REAL -> println(fieldEntry.real().asDouble())
                    DataTypes.DATE -> println("${fieldEntry.date().day()} / ${fieldEntry.date().month()} / ${fieldEntry.date().year()}")
                    DataTypes.TIME -> println("${fieldEntry.time().hour()} : ${fieldEntry.time().minute()} : ${fieldEntry.time().second()} : ${fieldEntry.time().millisecond()}")
                    DataTypes.INT -> println(fieldEntry.intValue())
                    DataTypes.UINT -> println(fieldEntry.uintValue())
                    DataTypes.ASCII -> println(fieldEntry.ascii())
                    DataTypes.ENUM -> println("${if(fieldEntry.hasEnumDisplay()) fieldEntry.enumDisplay() else fieldEntry.enumValue() }")
                    DataTypes.RMTES -> println(fieldEntry.rmtes())
                    DataTypes.ERROR -> println("(${fieldEntry.error().errorCodeAsString()})")
                    else -> println("")
                }
            }
        }
    }

}

fun main(args: Array<String>) {
    lateinit var consumer: OmmConsumer
    //lateinit  var config: OmmConsumerConfig
    val appClient = AppclientFieldListWalk()
    //lateinit var reqMsg:ReqMsg
    try {
        println("Starting Kotlin_Consumer_220 application")

        //OmmConsumer, OmmConsumerConfig creation and establish communication.
        consumer = EmaFactory.createOmmConsumer(EmaFactory.createOmmConsumerConfig().consumerName("Consumer_1"))

        println("Kotlin_Consumer_220: Send item request message")
        consumer.registerClient(EmaFactory.createReqMsg().serviceName("DIRECT_FEED").name("EUR="), appClient) //Subscribe for EUR= RIC from DIRECT_FEED service

        Thread.sleep(60000)

    } catch (excp: InterruptedException) {
        println(excp.message)
    } catch (excp: OmmException) {
        println(excp.message)
    } finally {
        /*consumer?.let {
            consumer.uninitialize()
        }*/
        consumer.uninitialize()
    }
}