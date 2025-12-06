package fan.dang.web

import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Attach a suspending handler to a Vert.x route.
 * The block runs on the Vert.x dispatcher and failures are forwarded to the route failure handler.
 */
fun Route.coroutineHandler(block: suspend (RoutingContext) -> Unit): Route =
    handler { ctx ->
        CoroutineScope(ctx.vertx().dispatcher()).launch {
            try {
                block(ctx)
            } catch (e: Exception) {
                ctx.fail(e)
            }
        }
    }
