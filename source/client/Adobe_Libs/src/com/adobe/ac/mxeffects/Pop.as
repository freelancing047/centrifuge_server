package com.adobe.ac.mxeffects
{

import flash.filters.BlurFilter;

import mx.effects.IEffectInstance;
import mx.effects.TweenEffect;
import com.adobe.ac.mxeffects.effectClasses.PopInstance;

public class Pop extends TweenEffect
{
	public function Pop( target:Object = null )
	{
		super( target );
		instanceClass = PopInstance;
	}
		
	[Inspectable(category="General", defaultValue="null")]
	public static var defaultDirection : String = "RIGHT";
	
	[Inspectable(category="General", defaultValue="null")]
	public static var defaultMode : String = "DOWN";
	
	[Inspectable(category="General", defaultValue="null")]
	public var siblings : Array;

	[Inspectable(category="General", defaultValue="null", enumeration="RIGHT,LEFT,TOP,BOTTOM")]
	public var direction : String;
	
	[Inspectable(category="General", defaultValue="null", enumeration="DOWN,UP")]
	public var mode : String;	
	
	[Inspectable(category="General", defaultValue="null")]
	public static var defaultBuildMode : String = "POPUP";
	
	[Inspectable(category="General", defaultValue="null", enumeration="POPUP,REPLACE,ADD,OVERWRITE")]
	public var buildMode : String;
	
	[Inspectable(category="General", defaultValue="false")]
	public var smooth : Boolean;

	[Inspectable(category="General", defaultValue="NaN")]
	public var distortion : Number;
	
	[Inspectable(category="General", defaultValue="false")]
	public var liveUpdate : Boolean = false;
	
	[Inspectable(category="General", defaultValue="0")]
	public var liveUpdateInterval : int = 0;	
		
	[Inspectable(category="General", defaultValue="null")]
	public var blur : BlurFilter;
	
	[Inspectable(category="General", defaultValue="null", enumeration="RIGHT,LEFT")]
	public var horizontalLightingLocation : String;
	
	[Inspectable(category="General", defaultValue="null", enumeration="TOP,BOTTOM")]
	public var verticalLightingLocation : String;
	
	[Inspectable(category="General", defaultValue="NaN")]
	public var lightingStrength : Number;		
		
	override protected function initInstance( instance : IEffectInstance ) : void
	{
		super.initInstance( instance );		
		var effectInstance : PopInstance = PopInstance( instance );	
		effectInstance.siblings = siblings;
		effectInstance.direction = direction;
		effectInstance.buildMode = buildMode;
		effectInstance.smooth = smooth;
		effectInstance.mode = mode;
		effectInstance.distortion = distortion;
		effectInstance.liveUpdate = liveUpdate;
		effectInstance.liveUpdateInterval = liveUpdateInterval;
		effectInstance.blur = blur;
		effectInstance.horizontalLightingLocation = horizontalLightingLocation;
		effectInstance.verticalLightingLocation = verticalLightingLocation;
		effectInstance.lightingStrength = lightingStrength;
	}
}

}