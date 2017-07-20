import React from 'react'
import PropTypes from 'prop-types';

import {requireNativeComponent, View} from 'react-native';

const RCTFaceTrackerView = requireNativeComponent('RCTFaceTrackerView', FaceTrackerView, {
  nativeOnly: {
    onChange: true,
    onReceiveFace: true
  }
});

export default class FaceTrackerView extends React.Component {

  static propTypes = {
    onFaceDetection: PropTypes.func,
    onNewFace: PropTypes.func
  }

  constructor(props) {
    super(props);
    this._onChange = this
      ._onChange
      .bind(this);

    this._onNew = this
      ._onNew
      .bind(this);

  }
  _onChange(event) {
    console.log("=CHANGEVENT", event.nativeEvent.face, event.nativeEvent)
    if (!this.props.onFaceDetection) {
      return;
    }
    this
      .props
      .onFaceDetection(event.nativeEvent);
  }
  _onNew(event) {
    console.log("=NEWEVENT", event.nativeEvent.face, event.nativeEvent)
    if (!this.props.onNewFace) {
      return;
    }
    this
      .props
      .onNewFace(event.nativeEvent);

  }
  render() {
    return <RCTFaceTrackerView
      {...this.props}
      onChange={this._onChange}
      onReceiveFace={this._onNew}/>
  }
}
