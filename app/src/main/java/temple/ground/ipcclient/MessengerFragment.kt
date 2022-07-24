package temple.ground.ipcclient

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import temple.ground.ipcclient.databinding.FragmentMessengerBinding

class MessengerFragment : Fragment(), View.OnClickListener, ServiceConnection {

    private lateinit var viewBinding: FragmentMessengerBinding

    // Messenger on the server
    private var serverMessenger: Messenger? = null

    // Messenger on the client
    private var clientMessenger: Messenger? = null

    private var bound: Boolean = false

    // Handle messages from the remote service
    var handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            // Update UI with remote process info
            val bundle = msg.data
            viewBinding.linearLayoutClientInfo.visibility = View.VISIBLE
            viewBinding.btnConnect.text = getString(R.string.disconnect)
            viewBinding.txtServerPid.text = bundle.getInt(PID).toString()
            viewBinding.txtServerConnectionCount.text = bundle.getInt(CONNECTION_COUNT).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentMessengerBinding
            .inflate(inflater, container, false)
            .also { viewBinding = it; it.btnConnect.setOnClickListener(this) }
            .root

    override fun onClick(v: View?) {
        if(bound){
            doUnbindService()
        } else {
            doBindService()
        }
    }

    private fun doBindService() {
        clientMessenger = Messenger(handler)
        Intent("messenger_palisade").apply {
            `package` = "temple.ground.aidlserver"
            activity?.applicationContext?.bindService(this, this@MessengerFragment, Context.BIND_AUTO_CREATE)
        }
        bound = true
    }

    private fun doUnbindService() {
        if (bound) {
            activity?.applicationContext?.unbindService(this)
            bound = false
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        serverMessenger = Messenger(service)
        // Ready to send messages to remote service
        sendMessageToServer()
    }

    override fun onServiceDisconnected(className: ComponentName) {
        clearUI()
        serverMessenger = null
    }

    private fun clearUI(){
        viewBinding.txtServerPid.text = ""
        viewBinding.txtServerConnectionCount.text = ""
        viewBinding.btnConnect.text = getString(R.string.connect)
        viewBinding.linearLayoutClientInfo.visibility = View.INVISIBLE
    }

    private fun sendMessageToServer() {
        if (!bound) return
        val message = Message.obtain(handler)
        val bundle = Bundle()
        bundle.putString(DATA, viewBinding.edtClientData.text.toString())
        bundle.putString(PACKAGE_NAME, context?.packageName)
        bundle.putInt(PID, Process.myPid())
        message.data = bundle
        message.replyTo = clientMessenger // we offer our Messenger object for communication to be two-way
        try {
            serverMessenger?.send(message)
        } catch (e: RemoteException) {
            e.printStackTrace()
        } finally {
            message.recycle()
        }
    }
}