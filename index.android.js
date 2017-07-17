import PropTypes from 'prop-types';
import {requireNativeComponent, View} from 'react-native';

class FaceTrackerView extends React.Component {

  static propTypes = {
    onChangeMessage: PropTypes.func
  }

  constructor(props) {
    super(props);
    this._onChange = this
      ._onChange
      .bind(this);
  }
  _onChange(event : Event) {
    if (!this.props.onChangeMessage) {
      return;
    }
    this
      .props
      .onChangeMessage(event.nativeEvent.message);
  }
  render() {
    return <RCTFaceTrackerView {...this.props} onChange={this._onChange}/>;
  }
}

module.exports = requireNativeComponent('RCTFaceTrackerView', FaceTrackerView,{
  nativeOnly: {onChange: true}
});