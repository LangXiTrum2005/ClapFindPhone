package com.example.findphonebyclap.ui.sound

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.findphonebyclap.R
import com.example.findphonebyclap.data.model.SoundItem
import com.example.findphonebyclap.databinding.ItemSoundGridBinding

class SoundAdapter(
    private val sounds: List<SoundItem>,
    private var selectedSoundId: String,
    private val onSoundSelected: (SoundItem) -> Unit
) : RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {

    private var playingSoundId: String? = null

    inner class SoundViewHolder(private val binding: ItemSoundGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SoundItem) {
            val context = binding.root.context
            binding.tvSoundTitle.text = context.getString(item.nameRes)
            binding.ivSoundIcon.setImageResource(item.iconRes)

            val isSelected = item.id == selectedSoundId
            val isPlaying = item.id == playingSoundId

            if (isSelected) {
                binding.cardSound.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.app_card_bg_selected)
                )
                binding.cardSound.strokeColor =
                    ContextCompat.getColor(context, R.color.primary_purple)
                binding.cardSound.strokeWidth = 4
            } else {
                binding.cardSound.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.app_card_bg)
                )
                binding.cardSound.strokeColor =
                    ContextCompat.getColor(context, R.color.app_card_border)
                binding.cardSound.strokeWidth = 2
            }

            binding.ivPlayIndicator.visibility = if (isPlaying) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                val previousSelected = selectedSoundId
                selectedSoundId = item.id
                notifyDataSetChanged()
                onSoundSelected(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val binding =
            ItemSoundGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SoundViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        holder.bind(sounds[position])
    }

    override fun getItemCount(): Int = sounds.size

    fun setSelectedSound(soundId: String) {
        selectedSoundId = soundId
        notifyDataSetChanged()
    }

    fun setPlayingSound(soundId: String?) {
        playingSoundId = soundId
        notifyDataSetChanged()
    }
}
