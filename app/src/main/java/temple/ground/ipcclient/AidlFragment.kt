package temple.ground.ipcclient

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import temple.ground.aidlserver.IPalisadeAIDL
import temple.ground.ipcclient.databinding.FragmentAidlBinding

class AidlFragment : Fragment(), View.OnClickListener, ServiceConnection {

    private var _binding: FragmentAidlBinding? = null

    private val binding get() = _binding!!

    private var connected: Boolean = false

    private var iRemoteService: IPalisadeAIDL? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View =
        FragmentAidlBinding
            .inflate(inflater, container, false)
            .also { _binding = it; it.btnConnect.setOnClickListener(this) }
            .root

    override fun onClick(v: View?) {
        connected = if (connected) {
            disconnectToRemoteService()
            binding.txtServerPid.text = ""
            binding.txtServerConnectionCount.text = ""
            binding.btnConnect.text = getString(R.string.connect)
            binding.linearLayoutClientInfo.visibility = View.INVISIBLE
            false
        } else {
            connectToRemoteService()
            binding.linearLayoutClientInfo.visibility = View.VISIBLE
            binding.btnConnect.text = getString(R.string.disconnect)
            true
        }
    }

    private fun connectToRemoteService() {
        val intent = Intent("aidl_palisade")
        val pack = IPalisadeAIDL::class.java.`package`
        pack?.let {
            intent.setPackage(it.name)
            activity?.applicationContext?.bindService(
                intent, this, Context.BIND_AUTO_CREATE
            )
        }
    }

    private fun disconnectToRemoteService() {
        if(connected){
            activity?.applicationContext?.unbindService(this)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        // Gets an instance of the AIDL interface,
        // which we can use to call on the service
        iRemoteService = IPalisadeAIDL.Stub.asInterface(service)
        binding.txtServerPid.text = iRemoteService?.pid.toString()
        binding.txtServerConnectionCount.text = iRemoteService?.connectionCount.toString()
        iRemoteService?.setDisplayedValue(
            context?.packageName,
            Process.myPid(),
            binding.edtClientData.text.toString())
        connected = true
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Toast.makeText(context, "IPC server has disconnected unexpectedly", Toast.LENGTH_LONG).show()
        iRemoteService = null
        connected = false
    }
}