//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: gmail_sync.proto

package vault.gmail_sync;

@kotlin.jvm.JvmName("-initializeprofileDocPopup")
public inline fun profileDocPopup(block: vault.gmail_sync.ProfileDocPopupKt.Dsl.() -> kotlin.Unit): vault.gmail_sync.GmailSyncOuterClass.ProfileDocPopup =
  vault.gmail_sync.ProfileDocPopupKt.Dsl._create(vault.gmail_sync.GmailSyncOuterClass.ProfileDocPopup.newBuilder()).apply { block() }._build()
public object ProfileDocPopupKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: vault.gmail_sync.GmailSyncOuterClass.ProfileDocPopup.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: vault.gmail_sync.GmailSyncOuterClass.ProfileDocPopup.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): vault.gmail_sync.GmailSyncOuterClass.ProfileDocPopup = _builder.build()

    /**
     * <pre>
     * CTA configuration for button.
     * </pre>
     *
     * <code>.vault.common.CTA cta = 1;</code>
     */
    public var cta: vault.common.Cta.CTA
      @JvmName("getCta")
      get() = _builder.getCta()
      @JvmName("setCta")
      set(value) {
        _builder.setCta(value)
      }
    /**
     * <pre>
     * CTA configuration for button.
     * </pre>
     *
     * <code>.vault.common.CTA cta = 1;</code>
     */
    public fun clearCta() {
      _builder.clearCta()
    }
    /**
     * <pre>
     * CTA configuration for button.
     * </pre>
     *
     * <code>.vault.common.CTA cta = 1;</code>
     * @return Whether the cta field is set.
     */
    public fun hasCta(): kotlin.Boolean {
      return _builder.hasCta()
    }

    /**
     * <pre>
     * No. of records belonging to tab.
     * </pre>
     *
     * <code>string title = 2;</code>
     */
    public var title: kotlin.String
      @JvmName("getTitle")
      get() = _builder.getTitle()
      @JvmName("setTitle")
      set(value) {
        _builder.setTitle(value)
      }
    /**
     * <pre>
     * No. of records belonging to tab.
     * </pre>
     *
     * <code>string title = 2;</code>
     */
    public fun clearTitle() {
      _builder.clearTitle()
    }
  }
}
public inline fun vault.gmail_sync.GmailSyncOuterClass.ProfileDocPopup.copy(block: vault.gmail_sync.ProfileDocPopupKt.Dsl.() -> kotlin.Unit): vault.gmail_sync.GmailSyncOuterClass.ProfileDocPopup =
  vault.gmail_sync.ProfileDocPopupKt.Dsl._create(this.toBuilder()).apply { block() }._build()

val vault.gmail_sync.GmailSyncOuterClass.ProfileDocPopupOrBuilder.ctaOrNull: vault.common.Cta.CTA?
  get() = if (hasCta()) getCta() else null

