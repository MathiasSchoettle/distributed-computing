package api

import akka.NotUsed
import akka.actor.typed.{ActorSystem, DispatcherSelector, Scheduler}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Source}
import akka.stream.typed.javadsl.ActorSink
import akka.stream.typed.scaladsl.ActorSource
import akka.util.Timeout
import api.Domain.Message
import api.GroupActor.{GroupActorCommand, GroupActorReply, NewMessageAvailable, SendMessage}
import api.grpc.{MessagingService, Message => MessageGrpc, User => UserGrpc}
import com.google.protobuf.empty.Empty

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class MessagingServiceImpl(system: ActorSystem[GroupActorCommand]) extends MessagingService {
	implicit private val timeout: Timeout = Timeout(5 seconds)
	implicit private val scheduler: Scheduler = system.scheduler // for Ask pattern
	implicit private val dispatcher: ExecutionContextExecutor = system.dispatchers.lookup(DispatcherSelector.defaultDispatcher()) // for future
	implicit private val materializer: Materializer = Materializer(system) // some "magic" for streams (Source, Sink, Flow, ...)

	override def test(in: MessageGrpc): Future[MessageGrpc] = {
		val future: Future[GroupActor.TestReply] = system.ask(replyTo => GroupActor.Test(Message(in.id, in.text, in.senderId), replyTo))
		future.map(actorReply => MessageGrpc.of(actorReply.message.id, actorReply.message.text, actorReply.message.senderId))
	}

	override def send(in: Source[MessageGrpc, NotUsed]): Future[Empty] = {
		val actorSink = ActorSink.actorRef[GroupActorCommand](
			system,
			GroupActor.UserAgentLoggedOff,
			ex => GroupActor.UserAgentError(ex)
		)
		in.map(msg => SendMessage(Message(msg.senderId, msg.text, msg.senderId))).runWith(actorSink)
		Future.successful(Empty.of())
	}


	override def receive(in: UserGrpc): Source[MessageGrpc, NotUsed] = {

		val actorSource = ActorSource.actorRef[GroupActorReply](
			completionMatcher = PartialFunction.empty,
			failureMatcher = PartialFunction.empty,
			bufferSize = 1000,
			overflowStrategy = OverflowStrategy.dropHead
		)

		val (mergeSink, mergeSource) = MergeHub.source[GroupActorReply].map {
			case NewMessageAvailable(msg) => MessageGrpc(msg.id, msg.text, msg.senderId)
		}.toMat(BroadcastHub.sink[MessageGrpc])(Keep.both).run()

		val actorRef = actorSource.to(mergeSink).run()
		system ! GroupActor.AddUserAgent(in.name, actorRef)

		mergeSource
	}
}
