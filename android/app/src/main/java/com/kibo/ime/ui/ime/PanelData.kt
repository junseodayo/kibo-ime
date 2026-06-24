package com.kibo.ime.ui.ime

/** Emoji panel content (spec §9). Glyphs are *content*, never UI chrome. */
object EmojiData {
    val categories: List<Pair<String, List<String>>> = listOf(
        "표정" to "😀 😃 😄 😁 😆 😅 😂 🙂 🙃 😉 😊 😇 🥰 😍 🤩 😘 😗 😚 😙 😋 😛 😜 🤪 😝 🤔 🤨 😐 😑 😶 🙄 😏 😬 😌 😔 😪 🤤 😴 🥱"
            .split(" "),
        "사람" to "👍 👎 👌 ✌️ 🤞 🤙 👏 🙌 🙏 💪 👀 👋 🤝 ✍️ 💅 🧠 👶 🧑 👨 👩 🧓 🙆 🙅 🤷 🤦"
            .split(" "),
        "동물" to "🐶 🐱 🐭 🐹 🐰 🦊 🐻 🐼 🐨 🐯 🦁 🐮 🐷 🐸 🐵 🐔 🐧 🐦 🦆 🦉 🐝 🦋 🐢 🐙 🐬 🐳"
            .split(" "),
        "음식" to "🍎 🍊 🍋 🍌 🍉 🍇 🍓 🍑 🍒 🥝 🍅 🥑 🌽 🥕 🍞 🧀 🍗 🍔 🍟 🍕 🍣 🍙 🍜 🍰 ☕ 🍺"
            .split(" "),
        "활동" to "⚽ 🏀 🏈 ⚾ 🎾 🏐 🏓 🏸 🥊 🎿 🎯 🎮 🎲 🎸 🎹 🎺 🎨 📷 🎬 🎤 🏆 🥇 ✈️ 🚗 🚀 ⛺"
            .split(" "),
        "기호" to "❤️ 🧡 💛 💚 💙 💜 🖤 🤍 💔 ❣️ 💕 💯 ✅ ❌ ⭐ 🔥 ✨ 🎉 ⚡ 💡 🔔 🚩 ♻️ ⚠️ ✔️ ➡️"
            .split(" "),
    )
}

/** Symbol panel content — includes ₩ € ¥, arrows, and a 일본어 기호 set (spec §9). */
object SymbolData {
    val categories: List<Pair<String, List<String>>> = listOf(
        "자주" to "! ? . , … · : ; ' \" ( ) [ ] { } - – — _ / \\ & @ # % * + = ~ ^ |"
            .split(" "),
        "통화" to "₩ \$ € ¥ £ ¢ ₿ ₫ ₽ ₹ ₺ ₴ ₦ ฿ ₱ № % ‰ ™ © ®"
            .split(" "),
        "화살표" to "← → ↑ ↓ ↔ ↕ ↖ ↗ ↘ ↙ ⇐ ⇒ ⇑ ⇓ ⇔ ▲ ▼ ◀ ▶ « » ‹ ›"
            .split(" "),
        "수학" to "+ − × ÷ = ≠ ≈ ± ∞ √ ∑ ∏ ∫ π ∆ ∂ ≤ ≥ ∈ ∉ ⊂ ⊃ ∪ ∩ ° ′ ″"
            .split(" "),
        "일본어 기호" to "、 。 「 」 『 』 【 】 〔 〕 〈 〉 《 》 ・ ー 〜 ※ 々 〆 ○ ● ◎ △ ▽ □ ◆"
            .split(" "),
    )
}
